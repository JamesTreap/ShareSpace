from selenium import webdriver
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
import time
import threading
import sys
import os
from dotenv import dotenv_values
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from flask import Flask
from dotenv import dotenv_values
from entities import db  # Use the shared db instance
from importlib import import_module
from sqlalchemy.orm import configure_mappers
import sqlite3
import re

# *********************************************
# MAKE SURE YOU INSTALL SELENIUM
# - I added it to the requirements.txt file
#
# Also make sure you add to your .env.potential file:
#  - SUPABASE_EMAIL="your_email@email.com"
#  - SUPABASE_PASSWORD="your_password"
# *********************************************

# These are all the tables names. We match them to the title tags on the <a> elements
table_names = ['bills', 'device_tokens', 'finance_summaries', 'payments', 'room_invitations', 'room_members', 'rooms', 'task_users', 'tasks', 'users']

def clear_sql_data():
    sql_dir = os.path.join(os.path.dirname(__file__), "sql_data")
    if os.path.exists(sql_dir):
        for root, dirs, files in os.walk(sql_dir, topdown=False):
            for file in files:
                os.remove(os.path.join(root, file))
            for dir in dirs:
                os.rmdir(os.path.join(root, dir))
        os.rmdir(sql_dir)
    os.makedirs(sql_dir)
    instance_dir = os.path.join(os.path.dirname(__file__), "instance")
    if os.path.exists(instance_dir):
        for root, dirs, files in os.walk(instance_dir, topdown=False):
            for file in files:
                os.remove(os.path.join(root, file))
            for dir in dirs:
                os.rmdir(os.path.join(root, dir))
        os.rmdir(instance_dir)
    os.makedirs(instance_dir)

def configure_driver():
    options = Options()
    download_dir = os.path.abspath("./sql_data/supabase_exports")
    prefs = {
        "download.default_directory": download_dir,
        "download.prompt_for_download": False,
        "download.directory_upgrade": True,
        "safebrowsing.enabled": True
    }
    options.add_experimental_option("prefs", prefs)
    options.add_argument("--log-level=3")  # Only fatal errors
    options.add_experimental_option("excludeSwitches", ["enable-logging"])
    options.add_argument("--window-size=1920,1080")
    driver = webdriver.Chrome(options=options)
    return driver



def wait_for_page_load(driver):
    try:
        WebDriverWait(driver, 30).until(
            EC.presence_of_element_located((By.XPATH, "//a[@title='bills']"))
        )
    except Exception:
        print("Dashboard did not load in time or selector is incorrect.")


def sign_in(driver):
    env_path = os.path.join(os.path.dirname(__file__), '.', '.env.potential')
    env = dotenv_values(env_path)

    email = env.get('SUPABASE_EMAIL')
    password = env.get('SUPABASE_PASSWORD')

    driver.get("https://supabase.com/dashboard/project/lspggdzemkkimofytawo/editor/30695")

    # Enter Email
    for _ in range(20):
        try:
            email_input = driver.find_element(By.ID, "email")
            break
        except:
            time.sleep(0.5)
    else:
        raise Exception("Email input not found")

    email_input.clear()
    email_input.send_keys(email)

    # Enter password
    for _ in range(20):
        try:
            password_input = driver.find_element(By.ID, "password")
            break
        except:
            time.sleep(0.5)
    else:
        raise Exception("Password input not found")

    password_input.clear()
    password_input.send_keys(password)

    # Click the sign-in button
    for _ in range(20):
        try:
            sign_in_btn = driver.find_element(By.XPATH, "//button[@type='submit' and @form='signIn-form']")
            break
        except:
            time.sleep(0.5)
    else:
        raise Exception("Sign In button not found")

    sign_in_btn.click()

    # Pretty, colored, boxed message for captcha
    print("\n\033[1;37;44m" + "="*60 + "\033[0m")
    print("\033[1;33;44m{:^60}\033[0m".format("Please solve the captcha in the browser window."))
    print("\033[1;33;44m{:^60}\033[0m".format("The script will continue after you solve the captcha."))
    print("\033[1;33;44m{:^60}\033[0m".format("DO NOT mouse over the screen after solving the captcha!"))
    print("\033[1;37;44m" + "="*60 + "\033[0m\n")

    wait_for_page_load(driver)


def get_table_links(driver, titles):
    table_links = []
    for title in titles:
        links = driver.find_elements(By.XPATH, f"//a[@title='{title}']")
        table_links.extend(links)
    return table_links


def download_sql_for_table(driver, link):
    driver.execute_script("arguments[0].scrollIntoView();", link)
    link.click()
    try:
        ellipsis_button = link.find_element(By.XPATH, ".//button[contains(@class, 'text-foreground-lighter') and contains(@class, 'transition-all') and contains(@class, 'text-transparent') and contains(@class, 'group-hover:text-foreground') and contains(@class, 'data-[state=open]:text-foreground')]")
        ellipsis_button.click()
        time.sleep(.2)
        try:
            export_div = driver.find_element(By.XPATH, "//div[contains(text(), 'Export data')]")
            ActionChains(driver).move_to_element(export_div).perform()
            time.sleep(.2)
            try:
                export_sql_span = driver.find_element(By.XPATH, "//span[contains(text(), 'Export table as SQL')]")
                export_sql_span.click()
            except Exception as e:
                print("\n\033[1;37;41m" + "="*60 + "\033[0m")
                print("\033[1;33;41m{:^60}\033[0m".format("DO NOT mouse over the screen! Exiting Program"))
                print("\033[1;37;41m" + "="*60 + "\033[0m\n")
                driver.quit()
                sys.exit(1)
        except Exception as e:
            print("\033[1;37;41m" + "="*60 + "\033[0m")
            print("\033[1;33;41m{:^60}\033[0m".format("DO NOT mouse over the screen! Exiting Program"))
            print("\033[1;37;41m" + "="*60 + "\033[0m\n")
            driver.quit()
            sys.exit(1)
    except Exception:
        print("  No matching ellipsis button found for this link.")
    

def download_sql_files(driver):
    def loading_animation(stop_event):
        spinner = ['|', '/', '-', '\\']
        idx = 0
        sys.stdout.write("\033[1;36m\nDownloading SQL files... ")
        sys.stdout.flush()
        while not stop_event.is_set():
            sys.stdout.write(spinner[idx % len(spinner)])
            sys.stdout.flush()
            time.sleep(0.1)
            sys.stdout.write('\b')
            idx += 1
        sys.stdout.write('Done!\033[0m\n')
        sys.stdout.flush()

    stop_event = threading.Event()
    loader_thread = threading.Thread(target=loading_animation, args=(stop_event,))
    loader_thread.start()

    try:
        table_links = get_table_links(driver, table_names)
        for link in table_links:
            download_sql_for_table(driver, link)
            time.sleep(1.5)
        time.sleep(3)  # Wait for all downloads to complete
    finally:
        stop_event.set()
        loader_thread.join()

        
def build_seed_file():
    try:
        sql_files = [f for f in os.listdir("./sql_data/supabase_exports") if f.endswith('.sql')]
        sql_files.sort() 
        with open("./sql_data/seed_db.sql", 'w') as outfile:
            outfile.write("PRAGMA foreign_keys = OFF;\n\n")
            for fname in sql_files:
                fpath = os.path.join("./sql_data/supabase_exports", fname)
                with open(fpath, 'r') as infile:
                    for line in infile:
                        if line.strip().startswith("INSERT INTO"):
                            # Only clean the heading section
                            m = re.match(r'(INSERT INTO\s+)("public"\.)?"?(\w+)"?\s*\((.*?)\)(.*)', line, re.DOTALL)
                            if not m:
                                outfile.write(line)
                                continue
                            insert_into, _, table, columns, rest = m.groups()
                            table = table.replace('"', '')
                            columns = ', '.join([col.strip().replace('"', '') for col in columns.split(',')])
                            # Replace all single quotes with two single quotes (aggressive, but will fix all cases)
                            cleaned_line = f"{insert_into}{table} ({columns}){rest}\n"
                            cleaned_line = re.sub(r"(?<=\S)'s", "''s", cleaned_line)
                            outfile.write(cleaned_line)
                            # print(f"Processing {fname}...")
                            # instance_dir = os.path.join(os.path.dirname(__file__), "instance")
                            # if os.path.exists(instance_dir):
                            #     for root, dirs, files in os.walk(instance_dir, topdown=False):
                            #         for file in files:
                            #             os.remove(os.path.join(root, file))
                            #         for dir in dirs:
                            #             os.rmdir(os.path.join(root, dir))
                            #     os.rmdir(instance_dir)
                            # os.makedirs(instance_dir)
                            # try:
                            #     create_db()
                            # except Exception as e:
                            #     print(f"Error creating database: {e} while processing {fname}")
                        else:
                            outfile.write(line)
                            print("fj;olfjsaduiopfsdpouifhidshfiuoshdaufhjiusdok")
            outfile.write("\nPRAGMA foreign_keys = ON;\n")
        # print(f"Formatting complete. {len(sql_files)} files combined.")
    except Exception as e:
        print(f"Error during file processing: {e}")
        return

def create_db():
    def create_app() -> Flask:
        app = Flask(__name__)
        
        # Use local database configuration
        cfg = dotenv_values(".env.potential")
        database_uri = cfg.get("LOCAL_DATABASE_URL", "sqlite:///local_shareSpace.db")
        
        app.config.update(
            SECRET_KEY = cfg.get("SECRET_KEY", "dev-secret-key"),
            SQLALCHEMY_DATABASE_URI = database_uri,
            SQLALCHEMY_TRACK_MODIFICATIONS = False,
        )

        # Initialize the shared db instance
        db.init_app(app)

        with app.app_context():
            # Load models and create schema
            for m in ("user", "room", "task", "finance"):
                import_module(f"entities.{m}")
            configure_mappers()
            db.create_all()
        return app

    def run_sql_script(db_path="instance/local_shareSpace.db", script_path="sql_data/seed_db.sql"):
        with open(script_path, "r", encoding="utf-8") as f:
            sql = f.read()

        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        cursor.executescript(sql)
        conn.commit()
        conn.close()

    create_app()
    run_sql_script()

if __name__ == "__main__":
    driver = configure_driver()
    sign_in(driver)
    clear_sql_data()
    download_sql_files(driver)
    
    print("\033[1;36mLoading database...\033[0m")
    build_seed_file()
    create_db()
    driver.quit()
    print("\033[1;32mDatabase loaded successfully!\033[0m")
