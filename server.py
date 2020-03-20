import socket 

HOST = ''                 # Symbolic name meaning all available interfaces
PORT = 9876              # Arbitrary non-privileged port
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((HOST, PORT))
print('Server Ready')
s.listen(1)
conn, addr = s.accept()
print ('Connected by', addr)

while 1:

    try:
        data = conn.recv(1024)
        decodedRequest = data.decode("utf-8")

        if not data: break
        print( "request: {}".format(decodedRequest) )

        response = "Recieved by python server!"
        conn.sendall(str.encode(response + " \r\n")) # turn it back into bytes 

    # Press ctrl-c or ctrl-d on the keyboard to exit
    except (KeyboardInterrupt, EOFError, SystemExit):
        break

conn.close()