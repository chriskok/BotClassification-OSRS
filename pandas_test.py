import pandas as pd

data = pd.read_csv('data/player_data_04-08-2020.csv')  

print(data.shape)

data.drop_duplicates(subset ="Name", 
                     keep = False, inplace = True) 

print(data.shape)
