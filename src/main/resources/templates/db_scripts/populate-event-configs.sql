INSERT INTO event_configurations (id, jsonb) VALUES
('767c364e-2eae-4e6c-bb55-ebcc68b7bf66', '{
 "id": "767c364e-2eae-4e6c-bb55-ebcc68b7bf66",
 "name": "CREATE_PASSWORD_EVENT",
 "active": true,
 "templates": [
   {
     "templateId": "263d4e33-db8d-4e07-9060-11f442320c05",
     "outputFormat": "text/plain",
     "deliveryChannel": "email"
   }
 ]
}'),
('0b1b7cac-f6fe-45ec-99f5-6758d54a51d7', '{
 "id": "0b1b7cac-f6fe-45ec-99f5-6758d54a51d7",
 "name": "RESET_PASSWORD_EVENT",
 "active": true,
 "templates": [
   {
     "templateId": "ed8c1c67-897b-4a23-a702-c36e280c6a93",
     "outputFormat": "text/html",
     "deliveryChannel": "email"
   }
 ]
}'),
('44b3e36f-c1a9-47e8-9607-1266decfad80', '{
 "id": "44b3e36f-c1a9-47e8-9607-1266decfad80",
 "name": "PASSWORD_CREATED_EVENT",
 "active": true,
 "templates": [
   {
     "templateId": "ce9e3e2c-669a-4491-a12f-e0fdad066191",
     "outputFormat": "text/plain",
     "deliveryChannel": "email"
   }
 ]
}'),
('c35b12ac-0958-4fc8-b9f4-e5a6c2347abf', '{
 "id": "c35b12ac-0958-4fc8-b9f4-e5a6c2347abf",
 "name": "PASSWORD_CHANGED_EVENT",
 "active": true,
 "templates": [
   {
     "templateId": "0ff6678f-53cd-4a32-9937-504c28f14077",
     "outputFormat": "text/plain",
     "deliveryChannel": "email"
   }
 ]
}'),
('2ed2ad69-7aac-404a-8cef-f8b99e7190cd', '{
 "id": "2ed2ad69-7aac-404a-8cef-f8b99e7190cd",
 "name": "USERNAME_LOCATED_EVENT",
 "active": true,
 "templates": [
   {
     "templateId": "d0ee371a-f3f7-407a-b6f3-714362db6240",
     "outputFormat": "text/plain",
     "deliveryChannel": "email"
   }
 ]
}') ON CONFLICT DO NOTHING;
