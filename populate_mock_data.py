import json

file_path = r'd:\TestProject\AIGym\Backend_AIGym\AIGym_Postman_Collection.json'

with open(file_path, 'r', encoding='utf-8') as f:
    collection = json.load(f)

def process_item(item):
    if 'item' in item:
        for sub_item in item['item']:
            process_item(sub_item)
    elif 'request' in item:
        req = item['request']
        url = req.get('url', '')
        url_str = url if isinstance(url, str) else url.get('raw', '')
        
        if isinstance(url, str):
            req['url'] = url.replace('/:id', '/1').replace('/:category', '/CHEST_MIDDLE')
        elif isinstance(url, dict) and 'raw' in url:
            url['raw'] = url['raw'].replace('/:id', '/1').replace('/:category', '/CHEST_MIDDLE')
            if 'path' in url:
                url['path'] = [p.replace(':id', '1').replace(':category', 'CHEST_MIDDLE') for p in url['path']]

        method = req.get('method', '').upper()
        if method in ['POST', 'PUT']:
            body = req.get('body', {})
            if body.get('mode') == 'raw':
                payload = {}
                if 'register' in url_str:
                    payload = {"fullName": "Nguyen Van A", "email": "test@example.com", "password": "password123"}
                elif 'login' in url_str:
                    payload = {"email": "test@example.com", "password": "password123"}
                elif 'verify-email' in url_str:
                    payload = {"email": "test@example.com", "otp": "123456"}
                elif 'refresh' in url_str:
                    payload = {"refreshToken": "YOUR_REFRESH_TOKEN"}
                elif 'exercises' in url_str:
                    payload = {
                        "name": "Hít đất",
                        "description": "Bài tập ngực cơ bản",
                        "primaryCategory": "CHEST_MIDDLE",
                        "difficulty": "EASY",
                        "equipment": "Không",
                        "isPublic": True
                    }
                elif 'food-items' in url_str:
                    payload = {
                        "name": "Ức gà luộc",
                        "caloriesPer100g": 165,
                        "protein": 31.0,
                        "carbs": 0.0,
                        "fat": 3.6
                    }
                elif 'nutrition-logs' in url_str:
                    payload = {
                        "foodItemId": 1,
                        "amount": 100.0,
                        "mealType": "LUNCH",
                        "loggedAt": "2026-06-07"
                    }
                elif 'progress-photos' in url_str:
                    payload = {
                        "imageUrl": "https://example.com/photo.jpg",
                        "weightAtTime": 70.5,
                        "bodyFatPercentage": 15.0,
                        "capturedAt": "2026-06-07",
                        "notes": "Looking good!"
                    }
                elif 'profiles' in url_str:
                    payload = {
                        "height": 170.0,
                        "weight": 70.0,
                        "targetWeight": 65.0,
                        "dailyCalorieGoal": 2000
                    }
                elif 'schedules' in url_str:
                    payload = {
                        "name": "Lịch tập tuần này",
                        "notes": "Cố gắng theo sát lịch",
                        "isActive": True
                    }
                elif 'workout-plans' in url_str:
                    payload = {
                        "title": "Kế hoạch tăng cơ bản",
                        "description": "Dành cho người mới",
                        "durationWeeks": 4,
                        "difficulty": "EASY",
                        "isPublic": True
                    }
                elif 'sessions' in url_str or 'workout-sessions' in url_str:
                    payload = {
                        "workoutPlanId": 1,
                        "startTime": "2026-06-07T08:00:00",
                        "endTime": "2026-06-07T09:00:00",
                        "totalCaloriesBurned": 300,
                        "notes": "Tập tốt",
                        "logs": [
                            {
                                "exerciseId": 1,
                                "workoutsets": 3,
                                "reps": 12,
                                "weight": 50.0,
                                "restTime": 60,
                                "note": "Cố gắng"
                            }
                        ]
                    }
                
                if payload:
                    body['raw'] = json.dumps(payload, indent=4, ensure_ascii=False)

if 'item' in collection:
    for folder in collection['item']:
        process_item(folder)

with open(file_path, 'w', encoding='utf-8') as f:
    json.dump(collection, f, indent=4, ensure_ascii=False)

print("Mock data injected successfully.")
