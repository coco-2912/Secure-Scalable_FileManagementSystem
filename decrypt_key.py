from cryptography.fernet import Fernet
import base64
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.backends import default_backend

def load_decrypted_key():
    password = b"my_secure_password"  # Must be the same password used for encryption

    # Load the encrypted key
    with open("encrypted_secret.key", "rb") as file:
        data = file.read()

    salt = data[:16]  # Extract the salt
    encrypted_key = data[16:]  # Extract the encrypted key

    # Recreate KDF
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=32,
        salt=salt,
        iterations=100000,
        backend=default_backend()
    )

    # Derive the encryption key again
    derived_key = base64.urlsafe_b64encode(kdf.derive(password))

    # Decrypt the original key
    fernet = Fernet(derived_key)
    decrypted_key = fernet.decrypt(encrypted_key)

    return decrypted_key

# Test decryption
key = load_decrypted_key()
print("Decrypted Key:", key)
