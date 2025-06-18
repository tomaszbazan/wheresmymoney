# WheresMyMoney
A budgeting app that allows users to track their expenses and income. Users can create categories for their expenses and income, and add transactions to those categories. The app will then display a summary of the user's transactions and show the user how much money they have left in each category.

To build the app, run the following command:

```
gradle clean build
docker build -t wheresmymoney:version .
```

To run the app, run the following command:

```
docker compose up --no-deps --build 
```
