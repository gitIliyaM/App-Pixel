# ? User Management System

<div align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.1.0-green?logo=spring" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-21-red?logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Redis-6-red?logo=redis" alt="Redis">
</div>

## ? �������� �������

����������� ������� ���������� �������������� � ������������ �������������:

- ? **���������� ��������������** ����� JWT ������
- ? **���������� ��������** ����� ��������������
- ? **���������� ����������** (email � ��������)
- ? **������ �����** ������������� � �����������
- ? **����������** ������ �������

## ? ��������������� ����

### �������� ����������
| ��������� | ���������� | ������ |
|-----------|------------|--------|
| Java | �������� ���� ���������� | 21 |
| Spring Boot | ������-��������� | 3.1.0 |
| PostgreSQL | �������� ���� ������ | 15 |
| Redis | ����������� � ������ | 6 |

### �������������� ����������

 �������������� ����������
    "Spring Security"
    "Liquibase"
    "MapStruct"
    "JJWT"
    "Testcontainers"
    "SpringDoc"
    "Lombok"

? ����������� ����������

? ������������
+ ������������� �������������� (Access + Refresh ������)
+ ����������� ������� ����� BCrypt
+ �������������� ���������� ��� �������� ���������

? ������� �����
������� 1: Docker

docker-compose up --build -d

������� 2: ��������� ������

1. ��������� ���������:
   cp .env.example .env
2. �������� � ���������:
   mvn spring-boot:run

? API ������������
�����	����	          ��������
POST	/api/auth/login	  �������������� ������������
GET	    /api/users	      ����� �������������
POST	/api/transfers	  ������� ������� ����� �������

? ������������� ������������: /swagger-ui.html

? �������� ������
{
"email": "user@mail.ru",
"phone": "79201234567",
"password": "test123",
"balance": 1000.00
}

? ����������
? /actuator/health - �������� ���������

? /actuator/metrics - ������� ������������������

? /actuator/cache - ���������� �����������

? ������������� �������
�������� ��:

Liquibase ��� ���������� ������

XML-����� � src/main/resources/db/changelog

������������:

��������� � application.yaml � .env

��������� Docker � ���������� ���������

�����������:

������ � logs/application.log

������ ��������� �������



 
