#!/bin/bash

# --- Configuration ---
# Supabase connection string from your .env file
DB_URL="postgresql://postgres:roommate_app_2025@db.lspggdzemkkimofytawo.supabase.co:5432/postgres"

# Directory for SQL files
SQL_DIR="."

# Temporary file for the raw Supabase export
RAW_EXPORT_FILE="${SQL_DIR}/supabase_export_raw.sql"

# Final formatted seed file
FINAL_SEED_FILE="${SQL_DIR}/seed_db.sql"

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# --- Script Execution ---

echo -e "${YELLOW}Starting Supabase data export and formatting process...${NC}"

# 1. Clean up old SQL files
echo "Step 1: Deleting old SQL files..."
rm -f "${SQL_DIR}"/*.sql
echo -e "${GREEN}Cleanup complete.${NC}"

# 2. Export data from Supabase
echo "Step 2: Exporting data from Supabase..."
# We use --data-only to get INSERT statements, and --inserts for row-by-row inserts.
# This makes it compatible with SQLite.
if pg_dump "$DB_URL" --data-only --inserts --schema=public > "$RAW_EXPORT_FILE"; then
    echo -e "${GREEN}Supabase data exported successfully to ${RAW_EXPORT_FILE}.${NC}"
else
    echo -e "${RED}Error: Failed to export data from Supabase. Please check your connection string and network access.${NC}"
    exit 1
fi

# 3. Format the exported data into the final seed file
echo "Step 3: Formatting data into ${FINAL_SEED_FILE}..."

# Start with PRAGMA foreign_keys = OFF
echo "PRAGMA foreign_keys = OFF;" > "$FINAL_SEED_FILE"
echo "" >> "$FINAL_SEED_FILE"

# Process the raw export file:
# - Filter for lines that start with "INSERT INTO"
# - Remove the "public." schema prefix
# - Remove all double quotes
# - Append the cleaned lines to the final seed file
grep '^INSERT INTO' "$RAW_EXPORT_FILE" | sed 's/public\.//g; s/"//g' >> "$FINAL_SEED_FILE"
echo "" >> "$FINAL_SEED_FILE"

# End with PRAGMA foreign_keys = ON
echo "PRAGMA foreign_keys = ON;" >> "$FINAL_SEED_FILE"

# 4. Clean up the raw export file
rm "$RAW_EXPORT_FILE"

echo -e "${GREEN}Formatting complete. The final seed file is ready at ${FINAL_SEED_FILE}.${NC}"
echo -e "${YELLOW}Process finished successfully!${NC}"
