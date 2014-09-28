1.
Get protoc
Windows:
Download
Other:
./configure
make

2.
Update "protocol.proto" file in this directory

3.
Windows:
generate.bat
Other:
./generate.sh

4.
Java:
Include java/ProtbufJava.jar and generated file in ./gen/java
c++:
Link to libprotoc.so (-lprotoc) and compiled file ./gen/cpp

5.
???

6.
Profit!!!
