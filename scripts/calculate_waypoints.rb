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

def calculate_waypoints_for_json(file, output)
	points_data = []
	result = ""
	points_with_distance = []
	data = File.read(file)
	json_data = JSON.parse(data)
	encoded_coords = json_data['routes'][0]['overview_polyline']['points']
	decoded_coords = Polylines::Decoder.decode_polyline(encoded_coords)
	old_x = 0.0
	old_y = 0.0
	count = 0
	totaldistance = 0
	decoded_coords.each{|x|
	if old_x == 0.0 || old_y == 0.0
		old_x = x[0]
		old_y = x[1]
		#print "[#{x[0]},#{x[1]},#{totaldistance}]\n"
		points_with_distance << [x[0],x[1],totaldistance]
		count+=1
		next
	else
		a = distance [old_x,old_y],[x[0],x[1]]
		totaldistance += a
		#print "[#{x[0]},#{x[1]},#{totaldistance}]\n"
		points_with_distance << [x[0],x[1],totaldistance] 
		old_x = x[0]
		old_y = x[1]
		count += 1
	end
end

# Warning! Uncompleted and malfuncioning code below !!
=begin
old_point_data = []
act_point_height = 0
act_coord = []
$points_with_distance.each{|x|
	if old_point_data == []
		old_point_data = x
		act_point_height = x[2]
		act_coord = x
		next
	else
		# Synonyms
		p1 = old_point_data
		p2 = x
		
		# Calculate distance difference and actual point height
		distance_difference = x[2]-old_point_data[2]
		act_point_height += distance_difference
		
		# Check that act_point_height > 100, if yes, then calculate n coefficient and associated coords.
		# Calculate n coefficient
		# Warning! Not completed!! Not working properly !!!
		if act_point_height >= 100
			act_coord = x
			n_left = 1/(p2[2]-p1[2])*100
			n_right = 1-n_left
			print "Warning!" if n_left > 1 or n_right > 1
			average_x = (p1[0]*n_left+p2[0]*n_right)
			average_y = (p1[1]*n_right+p2[1]*n_right)
			$points_every_one_hundred_meters << [average_x,average_y]
			act_point_height -= 100
		end
		# set old_point_data to current point before loading next point
		old_point_data = x
	end
}
print $points_every_one_hundred_meters
=end
