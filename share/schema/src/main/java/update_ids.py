import os
import json

def update_id_field(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        data = json.load(file)
    
    if '$id' in data and 'open-metadata.org' in data['$id']:
        data['$id'] = data['$id'].replace('open-metadata.org', 'mobigen.com')
        with open(file_path, 'w', encoding='utf-8') as file:
            json.dump(data, file, indent=2)
        print(f"Updated $id in {file_path}")

def update_ids_in_directory(directory):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith('.json'):
                file_path = os.path.join(root, file)
                update_id_field(file_path)

if __name__ == "__main__":
    directory = "/Users/jblim/Workspace/DataFabricPlatform/share/schema/src/main/resources/json/schema"
    update_ids_in_directory(directory)
