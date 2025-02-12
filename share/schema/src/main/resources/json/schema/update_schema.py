import os

def update_schema(directory):
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".json"):
                file_path = os.path.join(root, file)
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                updated_content = content.replace('org.openmetadata.schema', 'com.mobigen.vdap.schema')
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(updated_content)

if __name__ == "__main__":
    directory = "/Users/jblim/Workspace/DataFabricPlatform/share/schema/src/main/resources/json/schema"
    update_schema(directory)
