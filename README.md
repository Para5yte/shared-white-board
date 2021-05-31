# shared-white-board
COMP90015 Distributed Systems - Shared White Board Assignment 2

Written fully using java

# Requirements
Java SDK 16
Javafx

# How to run
javafx is provided in the root directory as javafx however you can use your own javafx library

To run the server
```bash
java -p "javafx\lib" --add-modules "javafx.controls,javafx.fxml,javafx.swing" -jar server.jar <portnumber>
```
or
```bash
java -p "path to your own javafx lib" --add-modules "javafx.controls,javafx.fxml,javafx.swing" -jar server.jar <portnumber>
```

Then create a white board session

```bash
java -p "javafx\lib" --add-modules "javafx.controls,javafx.fxml,javafx.swing" -jar CreateWhiteBoard.jar localhost <portnumber> <username>
```
or
```bash
java -p "path to your own javafx lib" --add-modules "javafx.controls,javafx.fxml,javafx.swing" -jar JoinWhiteBoard.jar localhost <portnumber> <username>
```

Then join a white board session

java -p "javafx\lib" --add-modules "javafx.controls,javafx.fxml,javafx.swing" -jar JoinWhiteBoard.jar localhost <portnumber> <username>
```
or
```bash
java -p "path to your own javafx lib" --add-modules "javafx.controls,javafx.fxml,javafx.swing" -jar JoinWhiteBoard.jar localhost <portnumber> <username>
```

# Known Bugs
1. If you click a tool and hold, then release anywhere out of the tool bar, the previously selected tool will still be in use
2. The temparory canvas may keep a drawing sometimes, unable to replicate since this has only happened once
3. When you kick a user, the "EXIT" protocol doesn't run properly which leaves the user who was kicked still in the session,
 thus the connected users array isn't updated as it should.
