require 'json'
require 'find'
require 'polylines'
dirs = ["climb_directions","climb_directions/json", "climb_directions/coords", "climb_directions/txt", "climb_directions/fragments_100m", "climb_directions/elevations"]
dirs.each{|x| Dir.mkdir(x) unless Dir.exist?(x) }

# Coords Array Structure
# x_coord,y_coord,distance_from_start,elevation
# coords[i][0] = x
# coords[i][1] = y
# coords[i][2] = distance
# coords[i][3] = elevation
# Not all fields are required.

$global_sql = "";
$global_ary = "";
$elevations_reader_path = "D:/Projekty/SRTM/"
$cur_dir = File.expand_path(File.dirname(__FILE__))

#** Calculate distance between two coordinates

def distance loc1, loc2
  rad_per_deg = Math::PI/180  # PI / 180
  rkm = 6372.8                  # Earth radius in kilometers
  rm = rkm * 1000             # Radius in meters
  dlat_rad = (loc2[0]-loc1[0]) * rad_per_deg  # Delta, converted to rad
  dlon_rad = (loc2[1]-loc1[1]) * rad_per_deg
  lat1_rad, lon1_rad = loc1.map {|i| i * rad_per_deg }
  lat2_rad, lon2_rad = loc2.map {|i| i * rad_per_deg }
  a = Math.sin(dlat_rad/2)**2 + Math.cos(lat1_rad) * Math.cos(lat2_rad) * Math.sin(dlon_rad/2)**2
  c = 2 * Math::atan2(Math::sqrt(a), Math::sqrt(1-a))
  rm * c # Delta in meters
end

#** Calculate waypoints from JSON file
#** Return coordinates array

def calculate_waypoints_for_json(file)
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
			points_with_distance << [x[0],x[1],totaldistance]
			count+=1
			next
		else
			a = distance [old_x,old_y],[x[0],x[1]]
			totaldistance += a
			points_with_distance << [x[0],x[1],totaldistance] 
			old_x = x[0]
			old_y = x[1]
			count += 1
		end
	}
	return points_with_distance
end

def generate_sql(pointsarray,climb_id)
	$global_ary = ""
	s = "insert into climb_curves (id,nr,x,y,d,e) values\n"
	pointsarray.each{|x|
		s << "(#{climb_id},#{x[3]},#{x[0]},#{x[1]},#{x[2]},0),\n"
		$global_ary << "#{x[0]} #{x[1]} #{x[2]}\n"
	}
	s[-2] = ";"
	File.open("climb_directions/coords/climb#{climb_id}.txt","wb"){|w| w.write($global_ary) }
	print "Climb #{climb_id} parsed.\n"
	return s
end

def generate_sql_for_file(filename)
	climb_id = filename.split("climb")[2].split(".json")[0]
	$global_sql << generate_sql(calculate_waypoints_for_json(filename),climb_id)
end

#** Read coords from filename to an array

def read_coords_from_file(filename)
	coords = []
	str = File.read(filename)
	str.each_line{|x|
		next if x.size < 3
		next unless x.include?("\x20")
		a = x.split("\x20")
		coords << [a[0].to_f,a[1].to_f,a[2].to_f]
	}
	return coords
end

#** Split coordinates by distance
#** coords => Coordinates array
#** distance => Distance of every point (default=100m)

def split_by_distance(coords, distance=100)
	new_coords = []
	max_count = coords[-1][2]
	if max_count%100 == 0
		max_count = max_count+0.01
	end
	act_nr = 0
	count = 0
	old_ary = coords[0]
	# Add first coordinate
	new_coords << coords[0]
	coords.each{|x|
		if x[2] > act_nr # If actual distance is bigger than specified distance..
			# Parse variable
			p1 = old_ary
			p2 = x
			n_left = 1/(p2[2]-p1[2])
			n_right = 1-n_left
			print "Warning! Invalid n coefficient (split_by_distance)!\nResult could be invalid.\n" if n_left > 1 or n_right > 1
			average_x = (p1[0]*n_left+p2[0]*n_right)
			average_y = (p1[1]*n_left+p2[1]*n_right)
			act_nr += distance
			new_coords << [average_x, average_y, act_nr]
			# Add last point and break the loop
			if act_nr > max_count
				new_coords[-1] = coords[-1]
			end
			old_ary = x
			old_dist = x[2]
		else
			old_ary = x
			old_dist = x[2]
		end
	}
	return new_coords
end

#** calculate points (every 100m)

def calculate_points(coords)
	points = 0
	last_elev = coords[0][3].to_f
	coords.each{|x|
		next if x[2] =="0" || x[2] == "0.0"
		elev_diff = x[3].to_f-last_elev
		#print "Nachylenie: #{elev_diff}%\n"
		if elev_diff < 0
			points -= elev_diff**2
		else
			points += elev_diff**2
		end
		last_elev = x[3].to_f
	}
	points = points/10
	return points
end

#** calculate points (every 200m)

def calculate_points_200m(coords)
	points = 0
	last_elev = coords[0][3].to_f
	count = 0
	coords.each{|x|
		count += 1
		next if count%2==1
		next if x[2] =="0" || x[2] == "0.0"
		elev_diff = x[3].to_f-last_elev
		#print "Nachylenie: #{elev_diff/2}%\n"
		if elev_diff < 0
			points -= (elev_diff/2)**2
		else
			points += (elev_diff/2)**2
		end
		last_elev = x[3].to_f
	}
	points = points/5
	return points
end

#** calculate elevation difference (m)

def calculate_elevdiff(coords)
	first_elev = points[0][3].to_f
	last_elev = points[-1][3].to_f
	return last_elev-first_elev
end

#** calculate total elevation difference (m)

def calculate_total_elevdiff(coords)
	total = 0
	last_elev = coords[0][3].to_f
	count = 0
	coords.each{|x|
		count += 1
		next if count == 1
		elev_diff = x[3].to_f-last_elev
		if elev_diff < 0
			total_elev -= elev_diff # - - = +
		else
			total_elev += elev_diff
		end
		last_elev = x[3].to_f
	}
	return total
end

#** export coords to string
#** coords => variable containing coordinates
def export_coords_array(coords)
	s = ""
	coords.each{|x|
		unless x == nil
			if x[3] == nil
				s << "#{x[0]} #{x[1]} #{x[2]}\n"
			else
				s << "#{x[0]} #{x[1]} #{x[2]} #{x[3]}\n"
			end
		end
	}
	return s
end

# [-- NOT READY --]
##** mix_spllited_coords = []
def mix_coords(coords1, coords2)
	coords = coords1+coords2
	coords = coords.sort_by {|x| x[2].to_f }
	return coords
end

##** cut_coords to get start and end of a climb; delete all points before minimal and after last elevation point

#** export coords to file

def export_coords_array_to_file(coords,filename)
	File.open(filename,'wb'){|w| w.write(export_coords_array(coords))}
end

#** import coords array
def import_coords_array(str)
	coords = []
	str.each_line{|x|
		next if x.size < 3
		next unless x.include?("\x20")
		a = x.split("\x20") # Split by space
		a.map!{|x| x.to_f }
		coords << a
	}
	return coords
end

#** import coords array from file
def import_coords_array_from_file(filename)
	data = File.read(filename)
	return import_coords_array(data)
end

#** get elevations from coords
def get_elevations_from_coords(coords)
	Dir.chdir($elevations_reader_path)
	export_coords_array_to_file(coords, "act_coords.txt")
	system("#{$elevations_reader_path}ElevationsReader.exe act_coords.txt act_result.txt")
	result = import_coords_array_from_file("act_result.txt")
	Dir.chdir($cur_dir)
	return result
end 

#** save global SQL (deprecated)

def save_global_sql
	print "Saving .sql..\n"
	File.open("climb_directions/database.sql","wb"){|w| w.write($global_sql) }
end

begin
	Find.find('climb_directions/json').each{|x|
		next if File.directory?(x) || x.split(".")[-1] != "json"
		a = calculate_waypoints_for_json(x)
		b = split_by_distance(a)
		c = mix_coords(a,b)
		print c
		exit
		print get_elevations_from_coords(a)
		exit
	}
	#save_global_sql
end
