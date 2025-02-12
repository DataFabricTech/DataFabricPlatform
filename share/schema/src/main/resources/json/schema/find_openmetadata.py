import os

def find_openmetadata(directory):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".json"):
                file_path = os.path.join(root, file)
                with open(file_path, 'r', encoding='utf-8') as f:
                    for line_num, line in enumerate(f, start=1):
                        if 'openmetadata' in line.lower():
                            print(f"Found in {file_path} at line {line_num}")

if __name__ == "__main__":
    directory = "/Users/jblim/Workspace/DataFabricPlatform/share/schema/src/main/resources/json/schema"
    find_openmetadata(directory)
