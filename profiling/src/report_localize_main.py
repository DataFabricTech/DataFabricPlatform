"""기존 프로파일링 HTML 리포트의 화면 문구를 한글화하는 CLI."""

import argparse
from pathlib import Path

from DataProfiling.process.report_html_localizer import localize_report_html_to_korean


def main():
    parser = argparse.ArgumentParser(description="프로파일링 HTML 리포트 한글화")
    parser.add_argument("--report_ids", nargs="+", required=True, help="한글화할 리포트 meta_id 목록")
    parser.add_argument("--output_dir", default="/profile-reports", help="리포트 디렉터리")
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    localized_count = 0
    for report_id in args.report_ids:
        raw_path = output_dir / f"{report_id}.html.raw"
        final_path = output_dir / f"{report_id}.html"
        legacy_path = output_dir / f"{report_id}.html.html"
        if not raw_path.is_file() and legacy_path.is_file():
            raw_path = legacy_path
        if not raw_path.is_file():
            print(f"리포트 없음: {raw_path}")
            continue
        localize_report_html_to_korean(raw_path, output_path=final_path)
        raw_path.unlink()
        localized_count += 1
    print(f"한글화 완료: {localized_count}개")


if __name__ == "__main__":
    main()
