# run project 
command docker-compose up -d

เมื่อโปรแกรมเริ่มรัน จะมี data json ที่เตรียมแบบสำรวจตามตัวอย่างไว้ให้อยู่แล้ว 1 ตัว

## Design Patterns ที่ใช้ในโปรเจค

1. MVC Pattern
   - Controller: อยู่ในแพ็คเกจ controller เช่น SurveyController รับคำขอจาก HTTP
   - Model: อยู่ในแพ็คเกจ model เช่น Survey, SurveyQuestion ใช้เก็บข้อมูล
   - Service: อยู่ในแพ็คเกจ service เช่น SurveyService ทำหน้าที่จัดการ business logic