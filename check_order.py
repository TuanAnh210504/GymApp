import json

with open(r'd:\TestProject\AIGym\Backend_AIGym\AIGym_Postman_Collection.json', 'r', encoding='utf-8') as f:
    col = json.load(f)

for item in col.get('item', []):
    print(f"Folder: {item.get('name')}")
    if 'item' in item:
        for sub in item['item']:
            print(f"  - {sub.get('name')}")
