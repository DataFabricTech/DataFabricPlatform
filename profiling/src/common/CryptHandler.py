import base64
import os
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from hashlib import sha256

class CryptHandler:

    @staticmethod
    def encodeWithcryptkey(cryptkey,data: str):
        cryptbytekey = sha256(cryptkey.encode()).digest()
        iv = os.urandom(12)
        data_bytes = bytearray(data, 'utf-8')

        cipher = Cipher(algorithms.AES(cryptbytekey), modes.GCM(iv))
        encryptor = cipher.encryptor()
        enc_data = encryptor.update(data_bytes) + encryptor.finalize()

        result = iv + encryptor.tag + enc_data

        return bytes(base64.b64encode(result)).decode(encoding='utf-8')

    @staticmethod
    def decodeWithcryptkey(cryptkey,b64_data: str) -> str:
        cryptbytekey = sha256(cryptkey.encode()).digest()
        enc_data = base64.b64decode(b64_data)

        iv = enc_data[:12]
        tag = enc_data[12:28]
        ciphertext = enc_data[28:]

        cipher = Cipher(algorithms.AES(cryptbytekey), modes.GCM(iv, tag))
        decryptor = cipher.decryptor()
        decoded_data = decryptor.update(ciphertext) + decryptor.finalize()

        return bytes(decoded_data).decode(encoding='utf-8')

    @staticmethod
    def decryptWithcryptkey(cryptkey,result: str) -> str:
        if str(result).find('ENC(') >= 0:
            result = CryptHandler.decodeWithcryptkey(cryptkey,str(result).replace('ENC(', '').replace(')', ''))
        return result

    @staticmethod
    def encryptWithcryptkey(cryptkey,data: str) -> str:
        return f"ENC({CryptHandler.encodeWithcryptkey(cryptkey,data)})"
