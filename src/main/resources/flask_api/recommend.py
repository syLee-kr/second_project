import pandas as pd
import requests
from geopy.distance import geodesic
from geopy.geocoders import Nominatim
import xml.etree.ElementTree as ET
import re
import os
from datetime import datetime

CACHE_FILE = "camping_data_cache.csv"
API_KEY = "YOUR_API_KEY"
BASE_URL = "http://apis.data.go.kr/B551011/GoCamping/basedList"

def clean_xml_response(response_text):
    response_text = re.sub(r"[^\x09\x0A\x0D\x20-\x7F]", "", response_text)
    response_text = response_text.replace("&", "&amp;")
    return response_text

def fetch_all_camping_data(force_update=False):
    if not force_update and os.path.exists(CACHE_FILE):
        last_modified = datetime.fromtimestamp(os.path.getmtime(CACHE_FILE))
        if (datetime.now() - last_modified).days < 1:
            return pd.read_csv(CACHE_FILE)

    all_items = []
    page_no = 1
    while True:
        params = {
            "serviceKey": API_KEY,
            "numOfRows": 1000,
            "pageNo": page_no,
            "MobileOS": "ETC",
            "MobileApp": "AppTest",
        }
        response = requests.get(BASE_URL, params=params)
        if response.status_code != 200:
            break

        try:
            cleaned_response = clean_xml_response(response.text)
            root = ET.fromstring(cleaned_response)

            for item in root.findall(".//item"):
                data = {child.tag: child.text or "정보 없음" for child in item}
                try:
                    data["mapX"] = float(data["mapX"])
                    data["mapY"] = float(data["mapY"])
                    all_items.append(data)
                except (ValueError, TypeError):
                    continue

            total_count = int(root.find(".//totalCount").text)
            if len(all_items) >= total_count:
                break
            page_no += 1
        except ET.ParseError:
            break

    camping_data = pd.DataFrame(all_items)
    camping_data.to_csv(CACHE_FILE, index=False)
    return camping_data

def recommend_camping_sites(user_address):
    geolocator = Nominatim(user_agent="geoapi")
    location = geolocator.geocode(user_address)
    if not location:
        return []

    user_lat, user_lon = location.latitude, location.longitude
    camping_data = fetch_all_camping_data()

    camping_data["distance"] = camping_data.apply(
        lambda row: geodesic((user_lat, user_lon), (row["mapY"], row["mapX"])).kilometers, axis=1
    )
    return camping_data.sort_values(by="distance").head(5).to_dict(orient="records")
