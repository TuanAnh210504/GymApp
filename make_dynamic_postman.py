import json

file_path = r'd:\TestProject\AIGym\Backend_AIGym\AIGym_Postman_Collection.json'

with open(file_path, 'r', encoding='utf-8') as f:
    collection = json.load(f)

folder_vars = {
    'Exercise': 'exercise_id',
    'FoodItem': 'food_id',
    'NutritionLog': 'log_id',
    'ProgressPhoto': 'photo_id',
    'WeeklySchedule': 'schedule_id',
    'WorkoutPlan': 'plan_id',
    'WorkoutSession': 'session_id'
}

def process_tests(events, id_var_name):
    for event in events:
        if event.get('listen') == 'test':
            exec_lines = event['script']['exec']
            if not any(id_var_name in line for line in exec_lines):
                injection = [
                    '',
                    'pm.test("Save ID to environment for next requests", function () {',
                    '    if (pm.response.code === 200 || pm.response.code === 201) {',
                    '        var jsonData = pm.response.json();',
                    '        if (jsonData.data && jsonData.data.id) {',
                    f'            pm.environment.set("{id_var_name}", jsonData.data.id);',
                    '            console.log("Saved ID: " + jsonData.data.id);',
                    '        }',
                    '    }',
                    '});'
                ]
                event['script']['exec'].extend(injection)

def process_item(item, current_folder=None):
    if 'item' in item:
        folder_name = item.get('name')
        for sub_item in item['item']:
            process_item(sub_item, folder_name)
    elif 'request' in item:
        req = item['request']
        url = req.get('url', '')
        url_str = url if isinstance(url, str) else url.get('raw', '')
        method = req.get('method', '').upper()
        
        id_var = folder_vars.get(current_folder, 'item_id')

        if method in ['POST', 'PUT'] and 'auth' not in url_str and 'profiles' not in url_str:
            if 'event' in item and method == 'POST':
                process_tests(item['event'], id_var)
            
            body = req.get('body', {})
            if body.get('mode') == 'raw' and body.get('raw'):
                try:
                    payload = json.loads(body['raw'])
                    # Thay đổi name / title
                    if 'name' in payload and '{{$timestamp}}' not in payload['name']:
                        payload['name'] = payload['name'] + ' {{$timestamp}}'
                    if 'title' in payload and '{{$timestamp}}' not in payload['title']:
                        payload['title'] = payload['title'] + ' {{$timestamp}}'
                    
                    body['raw'] = json.dumps(payload, indent=4, ensure_ascii=False)
                    
                    # Thay thế cứng các foreign keys (id) để xài ID chaining từ bài trước
                    body['raw'] = body['raw'].replace('"foodItemId": 1', '"foodItemId": {{food_id}}')
                    body['raw'] = body['raw'].replace('"exerciseId": 1', '"exerciseId": {{exercise_id}}')
                    body['raw'] = body['raw'].replace('"workoutPlanId": 1', '"workoutPlanId": {{plan_id}}')
                    
                except:
                    pass

        if isinstance(url, str):
            req['url'] = url.replace('/1', f'/{{{{{id_var}}}}}').replace('/:id', f'/{{{{{id_var}}}}}')
        elif isinstance(url, dict) and 'raw' in url:
            url['raw'] = url['raw'].replace('/1', f'/{{{{{id_var}}}}}').replace('/:id', f'/{{{{{id_var}}}}}')
            if 'path' in url:
                url['path'] = [p.replace('1', f'{{{{{id_var}}}}}').replace(':id', f'{{{{{id_var}}}}}') for p in url['path']]

if 'item' in collection:
    for folder in collection['item']:
        process_item(folder)

with open(file_path, 'w', encoding='utf-8') as f:
    json.dump(collection, f, indent=4, ensure_ascii=False)

print("Dynamic variables added successfully.")
