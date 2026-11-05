import os, sys
import logging
import logging.config

from src.configs.process_config import process_config


def set_logger():

    if os.path.exists(process_config.log_config_file):
        logging.config.fileConfig(process_config.log_config_file)
    else:
        print(f"로그 설정 파일 {process_config.log_config_file}를 찾을 수 없습니다.")
        sys.exit()
        
set_logger()


def get_logger():
    # 기존 ini 설정(logger name)과 호환 유지
    return logging.getLogger("profiling")

