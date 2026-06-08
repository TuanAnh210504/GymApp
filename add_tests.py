import json
import os
import shutil

file_path = r'd:\TestProject\AIGym\Backend_AIGym\AIGym_Postman_Collection.json'
backup_path = r'd:\TestProject\AIGym\Backend_AIGym\AIGym_Postman_Collection.json.backup'

with open(file_path, 'r', encoding='utf-8') as f:
    collection = json.load(f)

common_tests = [
    'pm.test("Successful request", function () {',
    '    pm.expect(pm.response.code).to.be.oneOf([200, 201, 204, 400, 401, 403, 404]); // Include error codes if testing edge cases, but for sunny day 2xx is expected',
    '    // To be strict: ',
    '    pm.expect(pm.response.code).to.be.oneOf([200, 201, 204]);',
    '});',
    '',
    'pm.test("Response time is acceptable", function () {',
    '    pm.expect(pm.response.responseTime).to.be.below(1500);',
    '});',
    '',
    'pm.test("Response is valid JSON if body is present", function () {',
    '    if (pm.response.text().length > 0) {',
    '        pm.response.to.be.json;',
    '    }',
    '});'
]

login_tests = common_tests + [
    '',
    'pm.test("Save JWT token to environment", function () {',
    '    if (pm.response.code === 200 && pm.response.text().length > 0) {',
    '        var jsonData = pm.response.json();',
    '        var token = jsonData.token || (jsonData.data && jsonData.data.token) || (jsonData.data && jsonData.data.accessToken) || jsonData.accessToken;',
    '        if (token) {',
    '            pm.environment.set("jwt_token", token);',
    '            console.log("Token saved!");',
    '        }',
    '    }',
    '});'
]

def add_events(item):
    if 'item' in item:
        # It's a folder
        for sub_item in item['item']:
            add_events(sub_item)
    elif 'request' in item:
        # It's a request
        tests = login_tests if "login" in item.get('name', '').lower() else common_tests
        
        event = {
            "listen": "test",
            "script": {
                "exec": tests,
                "type": "text/javascript",
                "packages": {}
            }
        }
        
        if 'event' not in item:
            item['event'] = []
            
        # check if test event already exists to avoid duplication
        test_event_exists = False
        for e in item['event']:
            if e.get('listen') == 'test':
                # Replace it or skip
                test_event_exists = True
                e['script']['exec'] = tests # Overwrite
                break
                
        if not test_event_exists:
            item['event'].append(event)

if 'item' in collection:
    for folder in collection['item']:
        add_events(folder)

shutil.copy2(file_path, backup_path)

with open(file_path, 'w', encoding='utf-8') as f:
    json.dump(collection, f, indent=4)

print("Automated tests have been successfully injected into the Postman collection.")
