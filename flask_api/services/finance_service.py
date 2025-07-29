# pylint: disable=all
from repository.finance_repo import FinanceRepo
from repository.room_repo import RoomRepo
from typing import List, Optional
from flask import abort
from entities import db
from datetime import datetime, timedelta
from entities.finance import Bill, FinanceSummary
from services.room_service import RoomService


class FinanceService:

    @staticmethod
    def get_room_bills_by_date(room_id: int, target_date: datetime) -> List[dict]:
        bills = FinanceRepo.get_bills_for_room_by_date(room_id, target_date)
        result = []
        for bill in bills:
            result.append({
                "type": "bill",
                "id": bill.id,
                "title": bill.title,
                "amount": float(bill.amount),
                "category": bill.category,
                "payer_user_id": bill.payer_user_id,
                "metadata": bill.meta_data,
                "deadline": bill.deadline.isoformat() if hasattr(bill, 'deadline') else None,
                "scheduled_date": bill.scheduled_date.isoformat() if bill.scheduled_date else None,
                "created_at": bill.created_at.isoformat(),
            })
        return result

    @staticmethod
    def get_room_financial_activity(room_id: int) -> List[dict]:
        bills = FinanceRepo.get_bills_for_room(room_id)
        payments = FinanceRepo.get_payments_for_room(room_id)

        merged = []

        for bill in bills:
            scheduled_sort_key = bill.scheduled_date or bill.created_at
            merged.append({
                "type": "bill",
                "id": bill.id,
                "title": bill.title,
                "amount": float(bill.amount),
                "category": bill.category,
                "payer_user_id": bill.payer_user_id,
                "meta_data": bill.meta_data,
                "scheduled_date": bill.scheduled_date.isoformat() if bill.scheduled_date else None,
                "created_at": bill.created_at.isoformat(),
                "_sort_key": scheduled_sort_key
            })

        for payment in payments:
            merged.append({
                "type": "payment",
                "id": payment.id,
                "amount": float(payment.amount),
                "payer_user_id": payment.payer_user_id,
                "payee_user_id": payment.payee_user_id,
                "created_at": payment.created_at.isoformat(),
                "_sort_key": payment.created_at
            })

        merged.sort(key=lambda x: x["_sort_key"], reverse=True)

        for item in merged:
            item.pop("_sort_key", None)

        return merged

    @staticmethod
    def validate_users(users: Optional[List[dict]], room_id: int) -> List[tuple]:
        if not users:
            abort(400, "At least one user is required.")

        validated_users = []
        for user in users:
            user_id = user.get("user_id")
            amount_due = user.get("amount_due")

            if not user_id or amount_due is None:
                abort(400, "Each user must include both 'user_id' and 'amount_due'.")

            try:
                amount_due = float(amount_due)
            except ValueError:
                abort(400, f"Invalid amount_due value '{amount_due}', it must be a number.")
            
            if amount_due < 0:
                abort(400, f"Amount due for user {user_id} cannot be negative.")
            
            try:
                user_id = int(user_id)
            except (ValueError, TypeError):
                abort(400, f"Invalid user_id value '{user_id}', it must be a valid user ID.")
            
            if not RoomService.validate_room_user(user_id, room_id):
                abort(404, f"User {user_id} does not belong to the specified room.")
            
            validated_users.append((user_id, amount_due))

        return validated_users

    @staticmethod
    def validate_input_create_bill(title: Optional[str], category: Optional[str], frequency: Optional[str], repeat: int):
        if not title:
            abort(400, "Title is required.")
        if not category:
            abort(400, "Category is required.")
        if frequency and frequency[-1] not in ['d', 'w', 'm']:
            abort(400, "Frequency must be in the format like '1d', '2w', or '3m'.")
        if frequency and repeat is None:
            abort(400, "Repeat is required and must be a number.")

        freq_value, unit = None, None
        if frequency:
            try:
                freq_value = int(frequency[:-1])
                freq_unit = frequency[-1]
                if freq_unit == 'd':
                    unit = 'days'
                elif freq_unit == 'w':
                    unit = 'weeks'
                elif freq_unit == 'm':
                    unit = 'months'
                else:
                    raise ValueError("Invalid frequency unit")
            except ValueError:
                abort(400, "Invalid frequency value or unit.")

        return freq_value, unit

    @staticmethod
    def create_bill_service(room_id: int, title: Optional[str], category: Optional[str], users: List[dict],
                            payer_id: int, amount: float, frequency: Optional[str], repeat: int):
        """Enhanced bill creation with better validation"""
        
        # Validate amount is positive
        if amount <= 0:
            abort(400, "Amount must be positive.")
        
        # Validate payer belongs to room
        if not RoomService.validate_room_user(payer_id, room_id):
            abort(404, "Payer does not belong to the specified room.")
        
        # Validate input
        freq_value, unit = FinanceService.validate_input_create_bill(title, category, frequency, repeat)
        user_data = FinanceService.validate_users(users, room_id)
        
        # Validate amounts
        total_user_amounts = sum([amount_due for _, amount_due in user_data])
        if abs(amount - total_user_amounts) > 0.01:  # Allow for small floating point differences
            abort(400, f"Total amount ({amount}) must equal sum of user amounts ({total_user_amounts}).")
        
        # Validate all amounts are positive
        for user_id, amount_due in user_data:
            if amount_due < 0:
                abort(400, f"Amount due for user {user_id} cannot be negative.")
        
        scheduled_dates = FinanceService.calculate_scheduled_dates(freq_value, unit, repeat)
        
        try:
            bills = []
            for scheduled_date in scheduled_dates:
                bill = FinanceRepo.create_bill(
                    title=title,
                    category=category,
                    amount=amount,
                    payer_user_id=payer_id,
                    frequency=frequency,
                    repeat=repeat,
                    room_id=room_id,
                    scheduled_date=scheduled_date,
                    meta_data={
                        "users": [{"user_id": user_id, "amount_due": amount_due} 
                                for user_id, amount_due in user_data]
                    }
                )
                bills.append(bill)
            
            # Update debt balances
            FinanceService.update_debt_balances_for_bill(room_id, payer_id, user_data)
            
            return bills[0]
            
        except Exception as e:
            db.session.rollback()
            raise e

    @staticmethod
    def calculate_scheduled_dates(freq_value: int, unit: str, repeat: int) -> List[datetime]:
        scheduled_dates = []
        now = datetime.now()
        scheduled_dates.append(now)
        if repeat > 0:
            for i in range(1, repeat):
                scheduled_dates.append(now + timedelta(**{unit: freq_value * i}))

        return scheduled_dates

    @staticmethod
    def create_payment_service(room_id: int, title: str, category: str, amount: float, payer_id: int, payee_id: int):
        if not RoomService.validate_room_users([payee_id, payer_id], room_id):
            abort(404, "Not all users belong to the specified room.")

        # Create the payment record
        payment = FinanceRepo.create_payment(
            room_id=room_id,
            title=title,
            category=category,
            amount=amount,
            payer_user_id=payer_id,
            payee_user_id=payee_id,
        )

        FinanceService.update_debt_balances_for_payment(room_id, payer_id, payee_id, amount)

        return payment

    @staticmethod
    def delete_bill(user_id: int, bill_id: int):
        if not FinanceService.is_user_in_room_of_bill(user_id, bill_id):
            abort(403, "User does not belong to the room.")
        
        bill = FinanceRepo.find_bill_by_id(bill_id)
        if not bill:
            abort(404, "Bill not found.")
        
        try:
            FinanceService.reverse_debt_balances_for_bill(bill)
            FinanceRepo.delete_bill(bill_id)
        except Exception as e:
            db.session.rollback()
            raise e

    @staticmethod
    def delete_payment(user_id: int, payment_id: int):
        if not FinanceService.is_user_in_room_of_payment(user_id, payment_id):
            abort(403, "User does not belong to the room.")
        
        payment = FinanceRepo.find_payment_by_id(payment_id)
        if not payment:
            abort(404, "Payment not found.")
        
        try:
            FinanceService.reverse_debt_balances_for_payment(payment)
            FinanceRepo.delete_payment(payment_id)
        except Exception as e:
            db.session.rollback()
            raise e

    @staticmethod
    def is_user_in_room_of_bill(user_id: int, bill_id: int) -> bool:
        bill = FinanceRepo.find_bill_by_id(bill_id)
        if not bill or bill.room_id is None:
            return False
        return RoomService.validate_room_user(user_id, bill.room_id)

    @staticmethod
    def is_user_in_room_of_payment(user_id: int, payment_id: int) -> bool:
        payment = FinanceRepo.find_payment_by_id(payment_id)
        if not payment or payment.room_id is None:
            return False
        return RoomService.validate_room_user(user_id, payment.room_id)
    
    # ============================================================================
    # DEBT TRACKING METHODS
    # ============================================================================

    @staticmethod
    def update_debt_balances_for_bill(room_id: int, payer_id: int, user_data: List[tuple]):
        """Update debt balances when a bill is created"""
        print(f"🔍 UPDATE_DEBT_BALANCES_FOR_BILL: payer_id={payer_id}, user_data={user_data}")

        try:
            # Cache summary objects to avoid multiple database queries
            summary_cache = {}
            
            def get_cached_summary(user_id):
                if user_id not in summary_cache:
                    summary_cache[user_id] = FinanceService._get_or_create_summary(room_id, user_id)
                    print(f"🔍 CACHED new summary for user {user_id}")
                else:
                    print(f"🔍 REUSING cached summary for user {user_id}")
                return summary_cache[user_id]
            
            for user_id, amount_due in user_data:
                print(f"🔍 Processing user {user_id}, amount_due={amount_due}, payer_id={payer_id}")
                if user_id != payer_id:
                    print(f"🔍 Calling add_debt({room_id}, {user_id}, {payer_id}, {amount_due})")
                    
                    # Use cached summaries instead of calling _get_or_create_summary again
                    debtor_summary = get_cached_summary(user_id)
                    creditor_summary = get_cached_summary(payer_id)  # This will reuse the same User 8 object
                    
                    creditor_id_str = str(payer_id)
                    debtor_id_str = str(user_id)
                    
                    # Simply add to existing amounts
                    current_debt_to_creditor = debtor_summary.owes.get(creditor_id_str, 0)
                    current_creditor_debts = creditor_summary.debts.get(debtor_id_str, 0)
                    
                    new_debt_amount = current_debt_to_creditor + amount_due
                    new_creditor_debt_amount = current_creditor_debts + amount_due
                    
                    FinanceService._update_debt_amount(debtor_summary, 'owes', creditor_id_str, new_debt_amount)
                    FinanceService._update_debt_amount(creditor_summary, 'debts', debtor_id_str, new_creditor_debt_amount)
            
            # Single commit after all updates
            db.session.commit()
            print(f"🔍 All debt updates committed successfully")
            
        except Exception as e:
            print(f"🔍 Error in update_debt_balances_for_bill: {e}")
            db.session.rollback()
            raise e

    @staticmethod
    def update_debt_balances_for_payment(room_id: int, payer_id: int, payee_id: int, amount: float):
        """Update debt balances when a payment is made"""
        FinanceService.reduce_debt(room_id, payer_id, payee_id, amount)

    @staticmethod
    def reverse_debt_balances_for_bill(bill):
        """Reverse debt balances when a bill is deleted"""
        if bill.meta_data and 'users' in bill.meta_data:
            for user_data in bill.meta_data['users']:
                user_id = user_data['user_id']
                amount_due = user_data['amount_due']
                
                if user_id != bill.payer_user_id:
                    # Reverse the debt
                    FinanceService.subtract_debt(bill.room_id, user_id, bill.payer_user_id, amount_due)
                    # FinanceService.reduce_debt(bill.room_id, user_id, bill.payer_user_id, amount_due)

    @staticmethod
    def reverse_debt_balances_for_payment(payment):
        """Reverse debt balances when a payment is deleted"""
        FinanceService.add_debt(payment.room_id, payment.payer_user_id, payment.payee_user_id, payment.amount)

    @staticmethod
    def add_debt(room_id: int, debtor_id: int, creditor_id: int, amount: float):
        """Add debt between two users - simple addition"""
        if amount <= 0:
            return
        
        if debtor_id == creditor_id:
            return
        
        try:
            # Get or create summaries for both users
            debtor_summary = FinanceService._get_or_create_summary(room_id, debtor_id)
            creditor_summary = FinanceService._get_or_create_summary(room_id, creditor_id)
            
            creditor_id_str = str(creditor_id)
            debtor_id_str = str(debtor_id)
            
            # Simply add to existing amounts
            current_debt_to_creditor = debtor_summary.owes.get(creditor_id_str, 0)
            current_creditor_debts = creditor_summary.debts.get(debtor_id_str, 0)
            
            new_debt_amount = current_debt_to_creditor + amount
            new_creditor_debt_amount = current_creditor_debts + amount
            
            FinanceService._update_debt_amount(debtor_summary, 'owes', creditor_id_str, new_debt_amount)
            FinanceService._update_debt_amount(creditor_summary, 'debts', debtor_id_str, new_creditor_debt_amount)
            
            # db.session.commit()
            
        except Exception as e:
            db.session.rollback()
            raise e
        

    @staticmethod
    def subtract_debt(room_id: int, debtor_id: int, creditor_id: int, amount: float):
        """Subtract debt between two users (for bill deletion)"""
        if amount <= 0:
            return
        
        if debtor_id == creditor_id:
            return
        
        try:
            # Get summaries for both users
            debtor_summary = FinanceSummary.query.filter_by(room_id=room_id, user_id=debtor_id).first()
            creditor_summary = FinanceSummary.query.filter_by(room_id=room_id, user_id=creditor_id).first()
            
            if not debtor_summary or not creditor_summary:
                return  # No debt to subtract
            
            creditor_id_str = str(creditor_id)
            debtor_id_str = str(debtor_id)
            
            # Simply subtract from existing amounts
            current_debt_to_creditor = debtor_summary.owes.get(creditor_id_str, 0)
            current_creditor_debts = creditor_summary.debts.get(debtor_id_str, 0)
            
            new_debt_amount = max(0, current_debt_to_creditor - amount)  # Don't go negative
            new_creditor_debt_amount = max(0, current_creditor_debts - amount)  # Don't go negative
            
            FinanceService._update_debt_amount(debtor_summary, 'owes', creditor_id_str, new_debt_amount)
            FinanceService._update_debt_amount(creditor_summary, 'debts', debtor_id_str, new_creditor_debt_amount)
            
            db.session.commit()
            
        except Exception as e:
            db.session.rollback()
            raise e

    @staticmethod
    def reduce_debt(room_id: int, debtor_id: int, creditor_id: int, amount: float):
        """Reduce debt between two users (payment scenario)"""
        print(f"🔍 REDUCE_DEBT: room_id={room_id}, debtor_id={debtor_id}, creditor_id={creditor_id}, amount={amount}")
    
        if amount <= 0:
            print(f"🔍 REDUCE_DEBT: Amount <= 0, returning")
            return
    
        if debtor_id == creditor_id:
            print(f"🔍 REDUCE_DEBT: Self-payment, returning")
            return
        
        try:
            print(f"🔍 REDUCE_DEBT: Getting summaries...")
            debtor_summary = FinanceService._get_or_create_summary(room_id, debtor_id)
            creditor_summary = FinanceService._get_or_create_summary(room_id, creditor_id)
            
            print(f"🔍 REDUCE_DEBT: Summaries obtained")
            creditor_id_str = str(creditor_id)
            debtor_id_str = str(debtor_id)
            
            current_debt = debtor_summary.owes.get(creditor_id_str, 0)
            print(f"🔍 REDUCE_DEBT: Current debt from {debtor_id} to {creditor_id}: {current_debt}")
            
            if current_debt >= amount:
                print(f"🔍 REDUCE_DEBT: Simple reduction case - {current_debt} >= {amount}")
                new_debt = current_debt - amount
                print(f"🔍 REDUCE_DEBT: New debt will be: {new_debt}")
                
                print(f"🔍 REDUCE_DEBT: Updating debtor owes...")
                FinanceService._update_debt_amount(debtor_summary, 'owes', creditor_id_str, new_debt)
                print(f"🔍 REDUCE_DEBT: Updating creditor debts...")
                FinanceService._update_debt_amount(creditor_summary, 'debts', debtor_id_str, new_debt)
                print(f"🔍 REDUCE_DEBT: About to commit...")
                
            elif current_debt > 0:
                print(f"🔍 REDUCE_DEBT: Overpayment case")
                # Partial payment that eliminates debt and creates reverse debt
                overpayment = amount - current_debt
                FinanceService._update_debt_amount(debtor_summary, 'owes', creditor_id_str, 0)
                FinanceService._update_debt_amount(creditor_summary, 'debts', debtor_id_str, 0)
                
                # Now creditor owes debtor the overpayment
                FinanceService._update_debt_amount(debtor_summary, 'debts', creditor_id_str, overpayment)
                FinanceService._update_debt_amount(creditor_summary, 'owes', debtor_id_str, overpayment)
            else:
                print(f"🔍 REDUCE_DEBT: No existing debt, creating reverse debt")
                # No existing debt, so this creates reverse debt
                FinanceService._update_debt_amount(debtor_summary, 'debts', creditor_id_str, amount)
                FinanceService._update_debt_amount(creditor_summary, 'owes', debtor_id_str, amount)
            
            db.session.commit()
            print(f"🔍 REDUCE_DEBT: Commit successful!")
            
        except Exception as e:
            print(f"🔍 REDUCE_DEBT: Error occurred: {e}")
            db.session.rollback()
            raise e

    @staticmethod
    def _get_or_create_summary(room_id: int, user_id: int) -> FinanceSummary:
        """Helper to get or create a FinanceSummary"""
        summary = FinanceSummary.query.filter_by(room_id=room_id, user_id=user_id).first()
        if not summary:
            summary = FinanceSummary(
                room_id=room_id,
                user_id=user_id,
                owes={},
                debts={}
            )
            db.session.add(summary)
        return summary

    @staticmethod
    def _update_debt_amount(summary: FinanceSummary, debt_type: str, other_user_id_str: str, amount: float):
        """Helper to update debt amounts with proper cleanup"""
        print(f"🔍 UPDATE_DEBT_AMOUNT: user_id={summary.user_id}, debt_type={debt_type}, other_user={other_user_id_str}, amount={amount}")
        
        debt_dict = (getattr(summary, debt_type) or {}).copy()
        print(f"🔍 UPDATE_DEBT_AMOUNT: Before - {debt_type}: {debt_dict}")
        
        # Clean up zero amounts and remove self-debts
        if amount <= 0 or other_user_id_str == str(summary.user_id):
            debt_dict.pop(other_user_id_str, None)
        else:
            debt_dict[other_user_id_str] = amount
        
        setattr(summary, debt_type, debt_dict)
        print(f"🔍 UPDATE_DEBT_AMOUNT: After - {debt_type}: {debt_dict}")

    # ============================================================================
    # QUERY AND UTILITY METHODS
    # ============================================================================

    @staticmethod
    def get_user_debt_summary(room_id: int, user_id: int) -> dict:
        """Get debt summary for a specific user in a room"""
        summary = FinanceSummary.query.filter_by(
            room_id=room_id, user_id=user_id
        ).first()
        
        print(f"🔍 GET_DEBT_SUMMARY: user_id={user_id}, summary found: {summary is not None}")
        if summary:
            print(f"🔍 GET_DEBT_SUMMARY: Raw owes: {summary.owes}")
            print(f"🔍 GET_DEBT_SUMMARY: Raw debts: {summary.debts}")
        
        if summary:
            # Filter out zero amounts and self-debts
            user_id_str = str(user_id)
            clean_owes = {k: v for k, v in (summary.owes or {}).items() 
                          if v > 0 and k != user_id_str}
            clean_debts = {k: v for k, v in (summary.debts or {}).items() 
                           if v > 0 and k != user_id_str}
            
            print(f"🔍 GET_DEBT_SUMMARY: Clean owes: {clean_owes}")
            print(f"🔍 GET_DEBT_SUMMARY: Clean debts: {clean_debts}")
            
            return {
                "owes": clean_owes,
                "debts": clean_debts
            }
        else:
            return {"owes": {}, "debts": {}}

    @staticmethod
    def get_room_members_with_debts(room_id: int) -> List[dict]:
        """Get all room members with their debt information"""
    
        
        from schemas.user_schemas import UserPublicSchema
        user_schema = UserPublicSchema(many=False)
        
        # Get room with members
        room = RoomService.get_room_by_id_with_members(room_id)
        if not room:
            return []

        # Build response with debt information for each member
        members_with_debts = []
        for room_member in room.members:
            member_user = room_member.user
            
            # Get basic user data
            member_data = user_schema.dump(member_user)
            member_user_id = member_user.id
            # Add debt information
            debt_summary = FinanceService.get_user_debt_summary(room_id,member_user_id)
            member_data['owes'] = debt_summary['owes']    # What this user owes TO others
            member_data['debts'] = debt_summary['debts']  # What others owe TO this user
            
            members_with_debts.append(member_data)

        return members_with_debts

    @staticmethod
    def validate_debt_consistency(room_id: int) -> List[str]:
        """Validate that debt records are consistent across users"""
        summaries = FinanceSummary.query.filter_by(room_id=room_id).all()
        issues = []
        
        for summary in summaries:
            user_id_str = str(summary.user_id)
            
            # Check owes consistency
            for creditor_id_str, amount in (summary.owes or {}).items():
                creditor_summary = FinanceSummary.query.filter_by(
                    room_id=room_id, user_id=int(creditor_id_str)
                ).first()
                
                if not creditor_summary:
                    issues.append(f"User {summary.user_id} owes {creditor_id_str} but creditor has no summary")
                    continue
                    
                creditor_debts = creditor_summary.debts or {}
                if creditor_debts.get(user_id_str, 0) != amount:
                    issues.append(f"Debt mismatch: User {summary.user_id} owes {creditor_id_str} ${amount}, "
                                f"but creditor records ${creditor_debts.get(user_id_str, 0)}")
        
        return issues

    @staticmethod
    def get_net_balances(room_id: int) -> dict:
        """Get simplified net balances for the room"""
        summaries = FinanceSummary.query.filter_by(room_id=room_id).all()
        net_balances = {}
        
        for summary in summaries:
            user_id = summary.user_id
            total_owes = sum((summary.owes or {}).values())
            total_owed = sum((summary.debts or {}).values())
            net_balance = total_owed - total_owes  # Positive means they're owed money
            
            if net_balance != 0:
                net_balances[user_id] = net_balance
        
        return net_balances

    @staticmethod
    def cleanup_debt_data(room_id: int):
        """Clean up zero amounts and self-debts from existing data"""
        summaries = FinanceSummary.query.filter_by(room_id=room_id).all()
        
        for summary in summaries:
            # COMPLETE WIPE FOR TESTING
            summary.owes = {}
            summary.debts = {}
            db.session.add(summary)
        
        db.session.commit()
        print(f"✅ Cleaned up debt data for room {room_id}")