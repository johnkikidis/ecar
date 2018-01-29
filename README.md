# ecar
Electric car power consumption prototype

Spring boot prototype application.

**A. Build and run the application using maven.**
```
mvn spring-boot:run
```

The server will start running exposing a public json API.


**C. Database**

The application needs an sql database to store and read its data.
Specify and connection string in src/resources/aplication.yml configuration file.
Also, the schema of the database is described in db/ folder.


**B. How to use**
 - Register power providers sending the name and the hourly power capacity (24 numbers, power per hour in daily basis)
 ```
 POST http://localhost:8080/providers
 {
     "name": "Elpedison AE",
     "power": [100,200,324,234.34,3435.23,100.23,900,399,599,185.63,36,109.11,56,23.34,997.99,987.23,234,100,99,98,100,122,152,785.34]
 }
 ```
 
 - Check the availability:
 ```
 GET http://localhost:8080/available?power=180
 ```
 
 
 - "Book" some power:
 ```
 POST http://localhost:8080/available?power=180&name=Giannis
 ```
 
