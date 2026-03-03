import requests
import json
import time

BASE_URL = "http://localhost:8080/api"

def print_result(step, response):
    status = "SUCCESS" if response.status_code in [200, 201] else "FAILED"
    print(f"[{status}] {step}: {response.status_code}")
    if status == "FAILED":
        print(f"Response: {response.text}")
        exit(1)
    return response

def verify_flow():
    print("Starting Mediscan Verification Flow...")
    
    # Generate unique user
    timestamp = int(time.time())
    username = f"testuser_{timestamp}"
    email = f"test_{timestamp}@example.com"
    password = "password123"

    # 1. Register
    print("\n--- 1. Registration ---")
    payload = {"username": username, "email": email, "password": password, "role": "USER"}
    resp = requests.post(f"{BASE_URL}/auth/register", json=payload)
    print_result("Register User", resp)

    # 2. Login
    print("\n--- 2. Login ---")
    payload = {"email": email, "password": password}
    resp = requests.post(f"{BASE_URL}/auth/login", json=payload)
    print_result("Login User", resp)
    token = resp.json().get("token")
    headers = {"Authorization": f"Bearer {token}"}
    print("Token acquired.")

    # 3. Create Medicine
    print("\n--- 3. Medicine Management ---")
    med_payload = {
        "name": f"Medicine {timestamp}",
        "description": "Test Description",
        "barcode": f"CODE-{timestamp}"
    }
    resp = requests.post(f"{BASE_URL}/medicines", json=med_payload, headers=headers)
    print_result("Create Medicine", resp)
    medicine = resp.json()
    medicine_id = medicine["id"]
    print(f"Created Medicine ID: {medicine_id}")

    # 4. Inventory
    print("\n--- 4. Inventory Management ---")
    inv_payload = {
        "medicineId": medicine_id,
        "quantity": 100,
        "lowStockThreshold": 10
    }
    resp = requests.post(f"{BASE_URL}/inventories", json=inv_payload, headers=headers)
    print_result("Initialize Inventory", resp)

    # Adjust Stock (Remove 5)
    resp = requests.post(f"{BASE_URL}/inventories/{medicine_id}/adjust?amount=-5", headers=headers)
    print_result("Adjust Stock (-5)", resp)
    new_qty = resp.json()["quantity"]
    if new_qty != 95:
        print(f"FAILED: Expected qty 95, got {new_qty}")
        exit(1)
    print("Stock adjustment verified.")

    # 5. Reminders
    print("\n--- 5. Reminder Management ---")
    # Set reminder for 1 day ahead
    reminder_time = "2025-12-31T08:00:00" 
    rem_payload = {
        "medicineId": medicine_id,
        "reminderTime": reminder_time
    }
    resp = requests.post(f"{BASE_URL}/reminders", json=rem_payload, headers=headers)
    print_result("Create Reminder", resp)
    reminder_id = resp.json()["id"]

    # Mark Taken
    resp = requests.post(f"{BASE_URL}/reminders/{reminder_id}/taken", headers=headers)
    print_result("Mark Reminder Taken", resp)
    if resp.json()["status"] != "TAKEN":
         print("FAILED: Status not updated to TAKEN")
         exit(1)

    # 6. Profile
    print("\n--- 6. User Profile ---")
    resp = requests.get(f"{BASE_URL}/users/me", headers=headers)
    print_result("Get Profile", resp)
    if resp.json()["username"] != username:
        print("FAILED: Profile username mismatch")
        exit(1)

    print("\n*** VERIFICATION SUCCESSFUL ***")

if __name__ == "__main__":
    try:
        verify_flow()
    except requests.exceptions.ConnectionError:
        print("FAILED: Connection refused. Is the backend running on localhost:8080?")
