from cryptography.fernet import Fernet
import base64
import os
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.backends import default_backend

# Step 1: Generate a random encryption key
key = Fernet.generate_key()

# Step 2: Use a password to derive another key
password = b"my_secure_password"  # Change this if needed
salt = os.urandom(16)  # Generate a random salt

# Key derivation function (PBKDF2)
kdf = PBKDF2HMAC(
    algorithm=hashes.SHA256(),
    length=32,
    salt=salt,
    iterations=100000,
    backend=default_backend()
)

derived_key = base64.urlsafe_b64encode(kdf.derive(password))  # Final encryption key

# Step 3: Encrypt the generated key
fernet = Fernet(derived_key)
encrypted_key = fernet.encrypt(key)

# Step 4: Store the salt and encrypted key
with open("encrypted_secret.key", "wb") as file:
    file.write(salt + encrypted_key)

print("Key encrypted and saved successfully!")
