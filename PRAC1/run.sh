javac -cp jade.jar -d classes src/Termostato/Termostato.java src/Termometro/Termometro.java
java -cp jade.jar:classes jade.Boot -gui
