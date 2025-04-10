#!/usr/bin/env python3
import base64
import argparse
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad

# 상수 정의
ALGORITHM = "AES"
SECRET = "j45DJWkJxdv+oxKv5zkuyXhGvi58Ycvt1tagcFbLfHQ="

def decode_secret_key(encoded_key):
    """Base64로 인코딩된 키를 디코딩하여 SecretKey 객체를 생성합니다."""
    decoded_key = base64.b64decode(encoded_key)
    return decoded_key

def encrypt(data):
    """AES 암호화를 수행합니다."""
    try:
        key = decode_secret_key(SECRET)
        cipher = AES.new(key, AES.MODE_ECB)
        padded_data = pad(data.encode(), AES.block_size)
        encrypted_bytes = cipher.encrypt(padded_data)
        return base64.b64encode(encrypted_bytes).decode()
    except Exception as e:
        print(f"암호화 중 오류 발생: {str(e)}")
        return None

def decrypt(encrypted_data):
    """AES 복호화를 수행합니다."""
    try:
        key = decode_secret_key(SECRET)
        cipher = AES.new(key, AES.MODE_ECB)
        decrypted_bytes = cipher.decrypt(base64.b64decode(encrypted_data))
        unpadded_data = unpad(decrypted_bytes, AES.block_size)
        return unpadded_data.decode()
    except Exception as e:
        print(f"복호화 중 오류 발생: {str(e)}")
        return None

DESC = '''AES 암호화/복호화 도구
이 프로그램은 AES(Advanced Encryption Standard) 암호화 알고리즘을 사용하여 문자열을 암호화하고 복호화하는 도구입니다.

주요 특징:
- AES-256 암호화 알고리즘 사용
- ECB (Electronic Code Book) 운영 모드
- Base64 인코딩 지원
- PKCS7 패딩 방식 사용

사용 예시:
1. 암호화:
python aes_crypto.py -e "안녕하세요"

2. 복호화:
python aes_crypto.py -d "암호화된_문자열"

주의사항:
- ECB 모드는 보안성이 낮을 수 있으므로, 실제 프로덕션 환경에서는 CBC나 GCM과 같은 더 안전한 모드 사용을 권장합니다.
- 시크릿 키는 안전하게 관리되어야 합니다.
- 암호화된 데이터는 Base64로 인코딩되어 출력되므로, 원본 데이터보다 길이가 길어질 수 있습니다.

의존성:
- pycryptodome: AES 암호화 구현을 위한 라이브러리
- Python 3.x 버전 필요'''

def main():
    parser = argparse.ArgumentParser(
        description=DESC, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument('-e', '--encrypt', help='암호화할 문자열')
    parser.add_argument('-d', '--decrypt', help='복호화할 문자열')
    
    args = parser.parse_args()
    
    if args.encrypt:
        result = encrypt(args.encrypt)
        if result:
            print(f"암호화 결과\n{result}")
    elif args.decrypt:
        result = decrypt(args.decrypt)
        if result:
            print(f"복호화 결과\n{result}")
    else:
        parser.print_help()

if __name__ == "__main__":
    main() 