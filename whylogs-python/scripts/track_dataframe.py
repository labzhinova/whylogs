"""
"""
from whylabs.logs.core.datasetprofile import dataframe_profile
import pandas as pd


if __name__ == '__main__':
    df = pd.read_csv('data/lending-club-accepted-10.csv')
    profile = dataframe_profile(df, name='lending_club')

    summary = profile.to_summary()