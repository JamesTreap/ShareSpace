from selenium import webdriver
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options

import time


options = Options()
driver = webdriver.Chrome(options=options)
driver.get("https://supabase.com/dashboard/project/lspggdzemkkimofytawo/editor/30695")

# Wait for page to load
email = "your_email@example.com"  # <-- Replace with your email
password = "your_password"        # <-- Replace with your password

# Wait for the email input to be present
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

# Wait for the password input to be present
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

# Find and click the sign-in button
for _ in range(20):
    try:
        sign_in_btn = driver.find_element(By.XPATH, "//button[@type='submit' and @form='signIn-form']")
        break
    except:
        time.sleep(0.5)
else:
    raise Exception("Sign In button not found")

sign_in_btn.click()

# Wait for login to complete
time.sleep(5)

# Now you can continue with your scraping/automation
buttons = driver.find_elements(By.ID, "radix-:rj7:")  
print("hi")
for button in buttons:
    print(button)
    time.sleep(5)