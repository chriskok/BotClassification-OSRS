import socket 
import pandas as pd
import csv
import os
import ast 
import sys
from datetime import datetime
import time

start = time.time()

location = "null"
now = datetime.now() # current date and time
# date_time = now.strftime("%m-%d-%Y_%H-%M")
date_time = now.strftime("%m-%Y")

data_path = 'data/player_data_{}.csv'.format(date_time)
labels = ['Name', 'Equip1', 'Equip2', 'Equip3', 'Equip4', 'Equip5', 'Equip6', 'Equip7', 'Equip8', 'Equip9', \
	'Equip10', 'Equip11', 'Equip12', 'Loc_x', 'Loc_y', 'Anim_id', \
		'Overall', 'Attack', 'Defence', 'Strength', 'Hitpoints', 'Ranged', 'Prayer', 'Magic', 'Cooking', 'Woodcutting', \
			'Fletching', 'Fishing', 'Firemaking', 'Crafting', 'Smithing', 'Mining', 'Herblore', 'Agility', 'Thieving', 'Slayer', \
				'Farming', 'Runecrafting', 'Hunter', 'Construction', 'Location']
def writeToCSV(data):
	with open(data_path, mode='w', newline='') as GE_data:
		GE_writer = csv.writer(GE_data, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
		GE_writer.writerow(labels)  # write field names
		GE_writer.writerow(data)


def appendToCSV(data):
	with open(data_path, mode='a', newline='') as GE_data:
		GE_writer = csv.writer(GE_data, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
		GE_writer.writerow(data)

def parseRequest(request_string):
	global location 

	try:
		parsed_array = []
		request_arr = request_string.split('\r\n')
		request_arr[1] = ast.literal_eval(request_arr[1]) # convert equipment list to array
		request_arr[2] = ast.literal_eval(request_arr[2]) # convert tuple to array

		parsed_array.append(request_arr[0]) # add the name
		parsed_array.extend(request_arr[1]) # add each item (should be 12)
		parsed_array.append(request_arr[2][0]) # add the location tile x value
		parsed_array.append(request_arr[2][1]) # add the location tile y value
		parsed_array.append(request_arr[3]) # add the animation id

		for i in range(4, 28):
			parsed_array.append(request_arr[i].split(',')[1]) # get only the level for each skill

		parsed_array.append(location)
		print(parsed_array)

		if os.path.isfile(data_path):
			appendToCSV(parsed_array)
		else:
			writeToCSV(parsed_array)
		
		return True
	except Exception as e:
		# print(e)
		return False

def getCheckedPlayers():
	player_names = ""
	first_line = True
	with open(data_path) as f:
		for row in f:
			if (first_line):
				first_line = False
				continue
			player_names += row.split(',')[0] + ","
	
	return player_names

def main():
	global location, start
	if(len(sys.argv) > 1):
		location = sys.argv[1]
		print('Location: {}'.format(location))
	else:
		print('Please select location based on expected skill - mining, woodcutting, none')
		return

	HOST = ''  # Symbolic name meaning all available interfaces
	PORT = 9876  # Arbitrary non-privileged port
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	s.bind((HOST, PORT))
	print('Server Ready')
	s.listen(1)
	conn, addr = s.accept()
	print ('Connected by', addr)

	past_checked_players = getCheckedPlayers()
	print("Restoring {} previously checked player names".format(past_checked_players.count(",")))
	conn.sendall(str.encode(past_checked_players + " \r\n")) # turn it back into bytes 

	req_count = 0
	while 1:

		try:
			data = conn.recv(1024)
			decodedRequest = data.decode("utf-8")
			if not data: break

			parse_success = parseRequest(decodedRequest)
			if (parse_success == True):
				req_count += 1


			response = "Recieved by python server!"
			if (req_count > 500): 
				print("Request count exceeded, stopping. Current time: {}".format(datetime.now()))
				response = "OUT"
			if (int(time.time() - start) > 7200): 
				print("Two hours passed, stopping. Current time: {}".format(datetime.now()))
				response = "OUT"
				start = time.time()
			conn.sendall(str.encode(response + " \r\n")) # turn it back into bytes 

		# Press ctrl-c or ctrl-d on the keyboard to exit
		except (KeyboardInterrupt, EOFError, SystemExit):
			break

	conn.close()
	

if __name__ == "__main__":
	
	# past_checked_players = getCheckedPlayers()
	# print(past_checked_players[0:100])
	main()