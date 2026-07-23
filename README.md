# Kimwanyi SACCO Management System

A Java EE / JSF web application for managing a community savings and credit cooperative.
Built with: **JSF 2.2 · Hibernate 5 · MySQL 8 · BCrypt · Maven · Tomcat**

---

## Tech Stack

| Layer      | Technology                        |
|------------|-----------------------------------|
| Frontend   | JSF 2.2 (Facelets), custom CSS    |
| Backend    | Java 17, JSF Managed Beans        |
| ORM        | Hibernate 5.6 (HQL)               |
| Database   | MySQL 8.x                         |
| Auth       | BCrypt password hashing           |
| Build      | Maven 3, WAR packaging            |
| Server     | Apache Tomcat 9/10                |

---

## Prerequisites

- **Java 17+**
- **Apache Maven 3.6+**
- **MySQL 8.x** running locally
- **Apache Tomcat 9** (or compatible)

---

## Setup Instructions

### 1. Create the Database

```sql
-- Run from the project root:
mysql -u root -p < kimwanyi_sacco/database/schema.sql
```

### 2. Configure Database Credentials

Edit `kimwanyi_sacco/src/main/resources/hibernate.cfg.xml` and set:

```xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/kimwanyi_sacco</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">your_password</property>
```

### 3. Seed the Default Admin Account

The system ships with no admin by default. Insert one manually:

```sql
-- Password: admin123  (BCrypt hash below)
INSERT INTO admins (username, full_name, password_hash, created_at, updated_at)
VALUES ('admin', 'System Administrator',
  '$2a$10$jyDYTP9WZV0Z3pDrP37T4eh25HtBKtwomiBcj68h4xKhNpQGXEbci',
  NOW(), NOW());
```

> To generate a different password hash: use `BCrypt.hashpw("yourpassword", BCrypt.gensalt())` or any online BCrypt generator.

### 4. Build the WAR

```bash
cd kimwanyi_sacco
mvn package -DskipTests
```

The WAR is produced at: `target/kimwanyi_sacco.war`

### 5. Deploy to Tomcat

Copy the WAR to Tomcat's webapps directory:

```bash
cp target/kimwanyi_sacco.war /path/to/tomcat/webapps/
```

Then start Tomcat. The app will be available at:

```
http://localhost:8080/kimwanyi_sacco/
```

---

## Application Pages

### Member Portal

| URL                              | Description                          |
|----------------------------------|--------------------------------------|
| `/login.xhtml`                   | Login page (admin & member)          |
| `/register.xhtml`                | Member self-registration             |
| `/member/dashboard.xhtml`        | Balance overview & transaction history |
| `/member/savings.xhtml`          | Deposit, withdraw, full statement    |
| `/member/loans.xhtml`            | Apply for loan, repay, view history  |

### Admin Panel

| URL                              | Description                          |
|----------------------------------|--------------------------------------|
| `/admin/dashboard.xhtml`         | KPI summary (members, savings, loans)|
| `/admin/members.xhtml`           | Register & manage member accounts    |
| `/admin/loans.xhtml`             | Approve / reject loan applications   |

---

## Business Rules Implemented

**Savings**
- Cannot withdraw below minimum balance of UGX 20,000
- 5% per annum interest applied monthly

**Loans**
- Maximum loan = 3× current savings balance
- Flat 10% interest on principal
- One active loan per member at a time
- Must repay fully before applying again
- Only admins can approve or reject

**Members**
- Unique National ID and Membership Number enforced
- Accounts deactivated (not deleted) by admin
- Admin accounts are structurally separate from member accounts

---

## Project Structure

```
kimwanyi_sacco/
├── database/
│   └── schema.sql                   # Full MySQL schema
├── src/main/
│   ├── java/org/pahappa/kimwanyi/
│   │   ├── bean/                    # JSF Managed Beans
│   │   │   ├── LoginBean.java
│   │   │   ├── RegisterBean.java
│   │   │   ├── MemberDashboardBean.java
│   │   │   ├── SavingsBean.java
│   │   │   ├── LoanBean.java
│   │   │   ├── AdminDashboardBean.java
│   │   │   ├── AdminLoansBean.java
│   │   │   └── AdminMembersBean.java
│   │   ├── dao/                     # Data Access Objects (Hibernate)
│   │   ├── model/                   # JPA Entity classes
│   │   ├── service/                 # Business logic layer
│   │   └── web/                     # Filters & Lifecycle Listeners
│   ├── resources/
│   │   └── hibernate.cfg.xml
│   └── webapp/
│       ├── login.xhtml
│       ├── register.xhtml
│       ├── admin/
│       │   ├── dashboard.xhtml
│       │   ├── members.xhtml
│       │   └── loans.xhtml
│       ├── member/
│       │   ├── dashboard.xhtml
│       │   ├── savings.xhtml
│       │   └── loans.xhtml
│       └── resources/css/
│           └── sacco.css
└── pom.xml
```

---

## Deliverables Checklist

- [x] Full source code
- [x] Working JSF web application (WAR)
- [x] README with setup instructions
- [x] Database schema (`database/schema.sql`)
- [x] Member registration, login, profile
- [x] Savings: deposit, withdraw, balance, statement
- [x] Loans: apply, approve/reject, repay, status
- [x] Interest calculation (5% p.a. savings, 10% flat loan)
- [x] Admin dashboard with summary counts and totals
- [x] Role-based access control (AuthFilter)
