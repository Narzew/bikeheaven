require 'json'
require 'polylines'

def distance loc1, loc2
  rad_per_deg = Math::PI/180  # PI / 180
  rkm = 6371                  # Earth radius in kilometers
  rm = rkm * 1000             # Radius in meters

  dlat_rad = (loc2[0]-loc1[0]) * rad_per_deg  # Delta, converted to rad
  dlon_rad = (loc2[1]-loc1[1]) * rad_per_deg
  
  lat1_rad, lon1_rad = loc1.map {|i| i * rad_per_deg }
  lat2_rad, lon2_rad = loc2.map {|i| i * rad_per_deg }

  a = Math.sin(dlat_rad/2)**2 + Math.cos(lat1_rad) * Math.cos(lat2_rad) * Math.sin(dlon_rad/2)**2
  c = 2 * Math::atan2(Math::sqrt(a), Math::sqrt(1-a))

  rm * c # Delta in meters
end

$points_data = []
$result = ""
$created_array = []
data = File.read('json/geocoded_route_bicycle.json')
json_data = JSON.parse(data)
encoded_coords = json_data['routes'][0]['overview_polyline']['points']
decoded_coords = Polylines::Decoder.decode_polyline(encoded_coords)
# Create points data
old_x = 0.0
old_y = 0.0
count = 0
totaldistance = 0
decoded_coords.each{|x|
	if old_x == 0.0 || old_y == 0.0
		old_x = x[0]
		old_y = x[1]
		#print "[#{x[0]},#{x[1]},#{totaldistance}]\n"
		$created_array << [x[0],x[1],totaldistance]
		count+=1
		next
	else
		a = distance [old_x,old_y],[x[0],x[1]]
		totaldistance += a
		#print "[#{x[0]},#{x[1]},#{totaldistance}]\n"
		$created_array << [x[0],x[1],totaldistance] 
		old_x = x[0]
		old_y = x[1]
		count += 1
	end
}
print $created_array
