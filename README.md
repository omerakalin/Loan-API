# Loan API - Java Backend Developer Case (Credit Module Challenge)

## 📖 Project Overview

This project is a **Loan API** developed for a banking application, allowing employees to create, list, and manage loans for customers. The API is built using **Java Spring Boot** and utilizes an **H2 database** for data storage. 

## 🛠️ Features

### Endpoints

1. **Create Loan**
   - Create a new loan for a given customer, amount, interest rate, and number of installments.
   - Validations:
     - Customer must have enough credit limit.
     - Number of installments can only be 6, 9, 12, or 24.
     - Interest rate must be between 0.1 and 0.5.
   - Total loan amount is calculated as: 
     
     > Total Amount = Amount * (1 + Interest Rate)
     
   - Due dates for installments are set to the first day of the following month.

2. **List Loans**
   - Retrieve a list of loans for a specific customer.
   - Optional filters include number of installments and payment status.

3. **List Installments**
   - Retrieve installments for a specific loan.

4. **Pay Loan**
   - Pay installments for a given loan and amount.
   - Installments must be paid wholly or not at all.
   - Payments are restricted to installments due within the next three months.
   - Returns information on the number of installments paid, total amount spent, and loan status.

### Database Structure

- **Customer**: 
  - `id`, `name`, `surname`, `creditLimit`, `usedCreditLimit`
  
- **Loan**: 
  - `id`, `customerId`, `loanAmount`, `numberOfInstallment`, `createDate`, `isPaid`
  
- **LoanInstallment**: 
  - `id`, `loanId`, `amount`, `paidAmount`, `dueDate`, `paymentDate`, `isPaid`

## 🔒 Security

- All endpoints are secured with an admin username and password.
- Bonus: Implemented role-based authorization allowing ADMIN users to manage all customers, while CUSTOMER role users can only manage their own loans.

## 🎁 Bonus Features

1. **Reward and Penalty Logic**:
   - Discounts for early payments:
     > Discount = Installment Amount * 0.001 * Days Before Due Date
   
   - Penalties for late payments:
     > Penalty = Installment Amount * 0.001 * Days After Due Date
     

## 🧪 Testing

- Unit tests have been implemented to ensure the functionality of the API.

## 📦 Installation

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Apache Maven
- H2 Database

### Steps to Run the Project

1. Clone the repository:
   >> git clone https://github.com/yourusername/loan-api.git
   cd loan-api

2. Build the project:
   >> mvn clean install

3. Run the application:
   >> mvn spring-boot:run

4. Access the API at:
   >> http://localhost:8080
You can use POSTMAN or H2 on Web browser.


📄 Documentation
For detailed API documentation, please refer to the API Documentation.

📖 Loan API Overview
The Loan API is designed to facilitate the management of personal loans for bank customers. It allows bank employees to efficiently create, track, and process loan payments. The API includes four main endpoints:

 Create Loan
> (POST /loans): This endpoint allows the creation of a new loan for a customer, ensuring they have sufficient credit limit and specifying the loan amount, interest rate, and number of installments.

 List Loans
> (GET /loans?customerId={id}): This endpoint retrieves all loans associated with a specific customer, enabling employees to view loan details and statuses.

 List Installments
> (GET /loans/{loanId}/installments): This endpoint provides a detailed list of installments for a specific loan, helping employees track payment schedules and amounts due.

 Pay Loan
> (POST /loans/{loanId}/pay): This endpoint processes payments for loan installments, allowing employees to pay off one or more installments based on the amount provided, while adhering to payment rules and restrictions.

Together, these endpoints streamline the loan management process, enhancing operational efficiency for bank employees.

*POSTMAN LINK : https://.postman.co/workspace/New-Workspace-for-API-demos~bd61b70c-c78d-4505-8184-51910356db43/collection/34264994-cf870648-7475-471c-a7fb-395db756dfa6?action=share&creator=34264994&active-environment=34264994-39db79ae-1316-45dc-9ea9-0ac23e6105c2*

👤 Author > 
***Ömer Şevki Akalın***

LinkedIn : 
> https://www.linkedin.com/in/ömer-şevki-akalın-90306714b/

GitHub :
> https://github.com/omerakalin

Credly :
> https://www.credly.com/users/omer-sevki-akalin

Thank you for checking out the Loan API project! Feel free to reach out for any questions or feedback.
