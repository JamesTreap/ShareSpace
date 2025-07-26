#!/usr/bin/env python
import subprocess
import os
import re

# --- Configuration ---
# Supabase connection string from your .env file
DB_URL = "postgresql://postgres:roommate_app_2025@db.lspggdzemkkimofytawo.supabase.co:5432/postgres"

# Directory for SQL files, relative to the script's location
SQL_DIR = os.path.dirname(__file__)

# Temporary file for the raw Supabase export
RAW_EXPORT_FILE = os.path.join(SQL_DIR, "supabase_export_raw.sql")

# Final formatted seed file
FINAL_SEED_FILE = os.path.join(SQL_DIR, "seed_db.sql")

def build_seed_file():
    """
    Fetches data from Supabase, formats it, and creates a seed_db.sql file.
    """
    print("--- Starting Supabase data export and formatting process ---")

    # 1. Export data from Supabase using pg_dump
    print(f"Step 1: Exporting data from Supabase to {RAW_EXPORT_FILE}...")
    try:
        # Using a list of arguments is safer for subprocess
        command = [
            'pg_dump',
            DB_URL,
            '--data-only',
            '--inserts',
            '--schema=public'
        ]
        with open(RAW_EXPORT_FILE, 'w') as f:
            # Run the command and redirect stdout to the file
            subprocess.run(command, check=True, text=True, stdout=f)
        print("Supabase data exported successfully.")
    except (subprocess.CalledProcessError, FileNotFoundError) as e:
        print(f"Error: Failed to export data from Supabase. Is pg_dump installed and in your PATH?")
        print(f"Details: {e}")
        return # Exit if export fails

    # 2. Format the exported data into the final seed file
    print(f"Step 2: Formatting data into {FINAL_SEED_FILE}...")
    try:
        with open(RAW_EXPORT_FILE, 'r') as infile, open(FINAL_SEED_FILE, 'w') as outfile:
            # Start with PRAGMA foreign_keys = OFF
            outfile.write("PRAGMA foreign_keys = OFF;\\n\\n")

            # Process each line of the raw export
            for line in infile:
                if line.strip().startswith("INSERT INTO"):
                    # Remove the "public." schema prefix and all double quotes
                    cleaned_line = re.sub(r'public\.', '', line)
                    cleaned_line = cleaned_line.replace('"', '')
                    outfile.write(cleaned_line)
            
            outfile.write("\\nPRAGMA foreign_keys = ON;\\n")
        print("Formatting complete.")
    except IOError as e:
        print(f"Error during file processing: {e}")
        return

    # 3. Clean up the raw export file
    finally:
        if os.path.exists(RAW_EXPORT_FILE):
            os.remove(RAW_EXPORT_FILE)
            print("Cleaned up temporary raw export file.")

    print("--- Process finished successfully! ---")

if __name__ == "__main__":
    build_seed_file()
