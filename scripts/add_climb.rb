require 'json'
require 'find'
require 'zlib'
require 'polylines'
dirs = ["climb_directions","climb_directions/json", "climb_directions/coords", "climb_directions/txt", "climb_directions/fragments_100m", "climb_directions/elevations"]
dirs.each{|x| Dir.mkdir(x) unless Dir.exist?(x) }
#$elev_data = Marshal.load(Zlib::Inflate.inflate(File.binread("elevations_associated.ncp")))
#$elev_nr = $elev_data.keys
$act_calc_failed = false

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
$request_str = ""
# ID of climbs which have multipart data
$multipart_ids = [2200,2205,2217,2242,2261,2271,2277,2280,2295,2303,2306,2322,2325,2342,2343,2351,2352,2355,2368,2375,2379,2380,2383,2384,2404,2405,2411,2414,2417,2424,2426,2451,2456,2501,2508,2510,2515,2529,2543,2560,2586,2602,2607,2625,2635,2639,2650,2655,2656,2666,2667,2670,2673,2676,2687,2688,2704,2717,2724,2726,2764,2770,2772,2774,2781,2788,2793,2794,2796,2804,2811,2834,2835,2845,2846,2848,2907,2930,2933,2953,2954,2959,2974,2991,2994,2998,2999,3006,3026,3028,3031,3068,3074,3087,3093,3102,3113,3120,3130,3138,3146,3148,3167,3170,3184,3202,3220,3230,3237,3271,3273,3284,3291,3311,3313,3314,3315,3328,3330,3331,3332,3337,3339,3346,3379,3388,3418,3433,3436,3441,3448,3472,3493,3508,3518,3533,3537,3541,3548,3553,3554,3562,3563,3565,3573,3574,3576,3578,3580,3583,3585,3592,3593,3607,3609,3648,3655,3656,3683,3698,3707,3708,3738,3752,3755,3756,3760,3766,3778,3811,3812,3839,3845,3848,3849,3889,3893,3907,3957,3964,4000,4002,4006,4027,4041,4046,4048,4060,4079,4110,4127,4173,4179,4181,4196,4226,4257,4320,4337,4353,4355,4370,4377,4399,4402,4438,4443,4463,4472,4485,4487,4489,4512,4599,4601,4602,4610,4611,4616,4631,4645,4687,4710,4712,4716,4721,4726,4731,4734,4808,4814,4816,4829,4841,4843,4844,4845,4859,4870,4871,4872,4873,4874,4875,4876,4877,4880,4892,4920,4924,4965,4966,4982,4988,5009,5038,5052,5066,5068,5086,5098,5099,5100,5133,5156,5173,5189,5190,5205,5214,5221,5226,5228,5327,5361,5369,5380,5381,5384,5406,5426,5430,5432,5450,5463,5473,5487,5490,5491,5495,5497,5498,5515,5520,5535,5563,5574,5596,5600,5601,5608,5629,5635,5641,5653,5663,5670,5674,5679,5689,5690,5694,5720,5739,5755,5758,5765,5791,5810,5812,5816,5820,5834,5841,5843,5847,5849,5851,5853,5854,5856,5857,5858,5861,5862,5865,5867,5869,5873,5881,5883,5907,5925,5932,5980,5995,6001,6015,6019,6052,6070,6084,6091,6107,6114,6161,6173,6178,6183,6192,6206,6252,6258,6270,6281,6282,6289,6335,6336,6347,6395,6400,6403,6418,6425,6485,6488,6520,6523,6545,6563,6564,6568,6593,6605,6610,6615,6618,6619,6620,6624,6625,6626,6627,6638,6649,6662,6685,6687,6744,6754,6761,6765,6776,6781,6792,6837,6851,6866,6867,6868,6885,6892,6942,6945,6954,6958,6960,6968,6974,7001,7014,7026,7038,7052,7065,7070,7077,7078,7082,7084,7087,7089,7090,7093,7098,7110,7111,7112,7116,7121,7122,7133,7144,7150,7158,7159,7160,7161,7168,7169,7170,7174,7175,7181,7184,7187,7188,7190,7193,7194,7261,7273,7277,7278,7280,7286,7287,7291,7298,7302,7339,7340,7341,7342,7351,7356,7367,7378,7394,7400,7408,7409,7411,7417,7422,7423,7436,7442,7465,7469,7482,7483,7493]
# Climbs that make trouble when parsing
$bad_climb_ids = [2200,2205, 2217, 2242, 2261, 2271, 2277, 2280, 2295, 2303, 2306, 2322, 2325, 2342, 2343, 2351, 2352, 2355, 2358, 2368, 2375, 2379, 2380, 2383, 2384, 2404, 2405, 2411, 2414, 2417, 2424, 2426, 2451, 2456, 2463, 2486, 2493, 2501, 2508, 2510, 2515, 2529, 2543, 2546, 2560, 2586, 2602, 2607, 2625, 2635, 2639, 2650, 2655, 2656, 2662, 2666, 2667, 2670, 2673, 2676, 2687, 2688, 2704, 2717, 2724, 2726, 2764, 2770, 2772, 2774, 2781, 2788, 2793, 2794, 2796, 2804, 2811, 2834, 2835, 2845, 2846, 2848, 2907, 2930, 2933, 2953, 2954, 2959, 2974, 2991, 2994, 2998, 2999, 3006, 3010, 3026, 3028, 3031, 3068, 3074, 3087, 3093, 3102, 3113, 3120, 3130, 3138, 3146, 3148, 3167, 3170, 3184, 3202, 3220, 3230, 3237, 3238, 3271, 3273, 3277, 3284, 3291, 3311, 3313, 3314, 3315, 3328, 3330, 3331, 3332, 3337, 3339, 3346, 3379, 3388, 3418, 3433, 3436, 3441, 3448, 3472, 3493, 3508, 3518, 3533, 3537, 3541, 3548, 3553, 3554, 3562, 3563, 3565, 3573, 3574, 3576, 3578, 3580, 3583, 3585, 3592, 3593, 3607, 3609, 3648, 3655, 3656, 3683, 3698, 3707, 3708, 3738, 3752, 3755, 3756, 3760, 3766, 3778, 3811, 3812, 3839, 3845, 3848, 3849, 3884, 3889, 3893, 3907, 3957, 3964, 4000, 4002, 4006, 4027, 4041, 4046, 4048, 4060, 4079, 4110, 4127, 4173, 4179, 4181, 4196, 4226, 4257, 4277, 4320, 4337, 4353, 4355, 4370, 4377, 4399, 4402, 4438, 4443, 4463, 4472, 4485, 4487, 4489, 4512, 4599, 4601, 4602, 4610, 4611, 4616, 4631, 4645, 4675, 4687, 4710, 4712, 4716, 4721, 4726, 4731, 4734, 4808, 4814, 4816, 4828, 4829, 4841, 4843, 4844, 4845, 4859, 4870, 4871, 4872, 4873, 4874, 4875, 4876, 4877, 4880, 4892, 4920, 4924, 4965, 4966, 4982, 4988, 5009, 5038, 5052, 5066, 5068, 5086, 5098, 5099, 5100, 5133, 5156, 5173, 5189, 5190, 5205, 5214, 5221, 5226, 5228, 5327, 5361, 5369, 5380, 5381, 5384, 5390, 5406, 5426, 5430, 5432, 5450, 5463, 5473, 5487, 5490, 5491, 5495, 5497, 5498, 5515, 5520, 5535, 5563, 5574, 5596, 5600, 5601, 5608, 5629, 5635, 5641, 5653, 5663, 5670, 5674, 5679, 5689, 5690, 5694, 5720, 5739, 5755, 5758, 5765, 5791, 5810, 5812, 5816, 5820, 5834, 5841, 5843, 5847, 5849, 5851, 5853, 5854, 5856, 5857, 5858, 5861, 5862, 5865, 5867, 5869, 5873, 5881, 5883, 5907, 5925, 5932, 5980, 5995, 6001, 6015, 6019, 6052, 6070, 6084, 6091, 6107, 6112, 6114, 6161, 6173, 6178, 6183, 6192, 6206, 6252, 6258, 6270, 6281, 6282, 6289, 6335, 6336, 6347, 6395, 6400, 6403, 6418, 6425, 6485, 6488, 6520, 6523, 6545, 6563, 6564, 6568, 6593, 6605, 6610, 6615, 6618, 6619, 6620, 6624, 6625, 6626, 6627, 6638, 6649, 6662, 6685, 6687, 6744, 6754, 6761, 6765, 6776, 6781, 6792, 6812, 6837, 6851, 6866, 6867, 6868, 6885, 6892, 6942, 6945, 6954, 6958, 6960, 6968, 6974, 7001, 7014, 7026, 7038, 7052, 7065, 7070, 7077, 7078, 7082, 7084, 7087, 7089, 7090, 7093, 7098, 7110, 7111, 7112, 7116, 7121, 7122, 7133, 7144, 7150, 7158, 7159, 7160, 7161, 7168, 7169, 7170, 7174, 7175, 7181, 7184, 7187, 7188, 7190, 7193, 7194, 7261, 7273, 7277, 7278, 7280, 7286, 7287, 7291, 7298, 7302, 7339, 7340, 7341, 7342, 7351, 7356, 7367, 7378, 7394, 7400, 7408, 7409, 7411, 7417, 7422, 7423, 7436, 7442, 7465, 7467, 7469, 7482, 7483, 7493, 7498, 7499, 7500, 7501, 7502, 7503, 7504, 7505, 7506, 7507, 7508, 7509, 7510, 7511, 7512, 7513, 7514, 7515, 7516, 7517, 7518, 7519, 7520, 7521, 7522, 7523, 7524, 7525, 7526, 7527, 7528, 7529, 7530, 7531, 7532, 7533, 7534, 7535, 7536, 7537, 7538, 7539, 7540, 7541, 7542, 7543, 7544, 7545, 7546]
print $bad_climb_ids.size
exit
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
	distance_ary = []
	new_coords = []
	max_count = coords[-1][2]
	if max_count%distance == 0
		max_count = max_count+0.01
	end
	act_nr = 0
	count = 0
	old_ary = coords[0]
	# Add first coordinate
	#new_coords << coords[0][0..2]
	coords.each{|x|
		if x[2] > act_nr # If actual distance is bigger than specified distance..
			next if distance_ary.include?(x[2].to_f) || distance_ary.include?(x[2].to_i)
			distance_ary << x[2].to_f
			# Parse variable
			p1 = old_ary
			p2 = x
			n_left = 1/(p2[2]-p1[2])
			n_right = 1-n_left
			if n_left>1 or n_right>1
				print "Warning! Invalid n coefficient (n_left=#{n_left}, n_right=#{n_right}) (split_by_distance)!\nResult could be invalid.\n" if n_left > 1 or n_right > 1
				#$stdin.gets
			end
			average_x = (p1[0]*n_left+p2[0]*n_right)
			average_y = (p1[1]*n_left+p2[1]*n_right)
			act_nr += distance
			new_coords << [average_x, average_y, act_nr]
			# Add last point and break the loop
			if act_nr > max_count
				new_coords[-1] = coords[-1][0..2]
			end
			old_ary = x
			old_dist = x[2]
		elsif x[2] == act_nr
			next if distance_ary.include?(x[2].to_f) || distance_ary.include?(x[2].to_i)
			distance_ary << x[2].to_f
			new_coords << [x[0],x[1],x[2]]
		else
			old_ary = x
			old_dist = x[2]
		end
	}
	return new_coords
end

#** calculate points (every 100m)

def calculate_points_100m(coords)
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
	coords.each{|x|
		next if x[2] =="0" || x[2] == "0.0"
		elev_diff = x[3].to_f-last_elev
		#print "Nachylenie: #{elev_diff}%\n"
		if elev_diff < 0
			points -= (elev_diff/2)**2
		else
			points += (elev_diff/2)**2
		end
		last_elev = x[3].to_f
	}
	points = points/10
	return points
end

#** calculate elevation difference (m)

def calculate_elevdiff(coords)
	first_elev = coords[0][3].to_f
	last_elev = coords[-1][3].to_f
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
			total -= elev_diff # - - = +
		else
			total += elev_diff
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

##** mix_spllited_coords
#** Mix two coordinate sets and sort it by distance

def mix_coords(coords1, coords2)
	coords = coords1+coords2
	coords = coords.sort_by {|x| x[2].to_f }
	return coords
end

#** cut_coords to get start and end of a climb; delete all points before minimal and after last elevation point
#** input coordinates require elevation information!

def cut_coords(coords)
	# Find minimal elevation
	act_min = coords[0][3]
	act_max = coords[0][3]
	act_index = -1
	min_index = 0
	max_index = 0
	coords.each{|x|
		act_index += 1
		if act_min > x[3]
			act_min = x[3]
			min_index = act_index
		end
		if act_max < x[3]
			act_max = x[3]
			max_index = act_index
		end
	}
	return coords[min_index..max_index]
end

#** recalculate distance (after cutting coords)
def recalculate_distance(coords)
	new_coords = []
	# Find first distance
	first_distance = coords[0][2].to_f
	coords.each{|x|
		new_distance = x[2].to_f - first_distance
		new_coords << [x[0],x[1],new_distance,x[3]]
	}
	return new_coords
end

#**remove points every 100m
# Not ready

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

#** get elevations from coords (SRTM data)
def get_elevations_from_coords_strm(coords, accuracy=3)
	Dir.chdir($elevations_reader_path)
	if accuracy == 1
		export_coords_array_to_file(coords, "actual_coords.txt")
		system("#{$elevations_reader_path}ElevationsReader_1arcsec.exe actual_coords.txt act_result.txt")
	elsif accuracy == 3
		export_coords_array_to_file(coords, "act_coords.txt")
		system("#{$elevations_reader_path}ElevationsReader.exe act_coords.txt act_result.txt")
	else
		print "Wrong accuracy! (get_elevations_from_coords)\n"
		exit
	end
	result = import_coords_array_from_file("act_result.txt")
	Dir.chdir($cur_dir)
	return result
end

#** get elevations from coords (google data)
def get_elevations_from_coords(coords, climb_id)
	new_coords = []
	coords = coords.compact
	coords.each{|x|
		next if x[0] == nil || x[1] == nil || x[2] == nil || x == nil
		lat_lng = "#{x[0].round(8)}_#{x[1].round(8)}"
		next if lat_lng == nil
		#print "#{lat_lng}\n"
		if $elev_data.include?(lat_lng)
			new_coords << [x[0],x[1],x[2],$elev_data[lat_lng]]
			#print "Parsed #{lat_lng}\n"
		else
			$act_calc_failed = true
			print "Failed to get elevation for climb ID: #{climb_id}\n"
			return coords
		end
	}
	return new_coords
end

#** add local climb
#** requires climb ID
def add_local_climb(id,dist=100)
	$act_calc_failed = false
	base_filename = "climb_directions/json/climb#{id}.json"
	if File.exist?(base_filename)
		# Get base coordinates
		base_coords = calculate_waypoints_for_json(base_filename).compact
		# Get coordinates + coordinates every 100m
		coords_with_distance = split_by_distance(base_coords,dist)
		# Get elevations for all coordinates
		coords_with_elevations = get_elevations_from_coords(coords_with_distance,id)
		if $act_calc_failed
			$bad_climb_ids << id.to_i
			print "Failed to get data for ID: #{id}\n"
			return
		end
		splitted_coords = recalculate_distance(cut_coords(coords_with_elevations)) rescue $act_calc_failed = true && $bad_climb_ids << id.to_i && return
		# Get encoded polyline
		splitted_coordinate_only = []
		coords_with_elevations.each{|x|
			splitted_coordinate_only << [x[0],x[1]]
		}
		s = ""
		splitted_coords.each{|x|
			s << "#{x[0]},#{x[1]}|"
		}
		s[-1] = ""
		#print "Encoded locations: #{s}\n"
		#export_coords_array_to_file(splitted_coords, "cutted_coords.txt")
		#encoded_polyline = Polylines::Encoder.encode_points(splitted_coordinate_only)
		#File.open("encoded_polyline.txt","wb"){|w| w.write(encoded_polyline) }
		#print "Encoded polyline: #{encoded_polyline}\n"
		# Get start coordinates
		start_x = splitted_coords[0][0].round(8)
		start_y = splitted_coords[0][1].round(8)
		# Get end coordinates
		finish_x = splitted_coords[-1][0].round(8)
		finish_y = splitted_coords[-1][1].round(8)
		total_distance = (splitted_coords[-1][2].round(1))/1000
		# Get start and finish elevation
		start_elevation = splitted_coords[0][3].round(3)
		finish_elevation = splitted_coords[-1][3].round(3)
		# Calculate elevation difference and total elevation difference
		elev_diff = calculate_elevdiff(splitted_coords).round(3)
		total_elev_diff = calculate_total_elevdiff(splitted_coords).round(3)
		# Calculate ranking points
		if dist == 100
			points = calculate_points_100m(splitted_coords).round(3)
		elsif dist == 200
			points = calculate_points_200m(splitted_coords).round(3)
		else
			print "Invalid dist parameter!\n"
			exit
		end
		slope = (elev_diff.to_f/total_distance.to_f)/10
		slope_str = "#{total_distance.round(1)}km #{slope.round(1)}%" 
		#points_200m = calculate_points_200m(splitted_coords)
		#points_500m = calculate_points_500m(splitted_coords)
		print "Elev_diff: #{elev_diff}\nTotal elev_diff: #{total_elev_diff}\nPoints: #{points}\n"
		#print "Elev_diff: #{elev_diff}\nTotal elev_diff: #{total_elev_diff}\nPoints: #{points}\nPoints (200m): #{points_200m}\nPoints (500m): #{points_500m}\n"
		# Not ready yet; Continue here
		dquery = "delete from climb_points where climb_id = #{id};delete from climb_slopes where climb_id = #{id};\n"
		query = "insert into climb_points(climb_id, point_nr,point_x,point_y) values\n"
		query2 = "insert into climb_slopes(climb_id, point_distance, elevation) values\n"
		query3 = "update climbs set start_x = #{start_x}, start_y = #{start_y}, end_x = #{finish_x}, end_y = #{finish_y}, points = #{points}, ascent = #{elev_diff}, tascent = #{total_elev_diff}, slope = \'#{slope_str}\', status = 2 where id = #{id};\n"
		count = 0
		splitted_coords.each{|x|
			count += 1
			query << "(#{id}, #{count}, #{x[0].round(8)},#{x[1].round(8)}),\n"
			query2 << "(#{id}, #{x[2].round(1)}, #{x[3].round(3)}),\n"
		}
		query[-2] = ";"
		query2[-2] = ";"
		query = dquery+query+query2+query3
		unless $act_calc_failed
			$global_sql << query
		else
			$bad_climb_ids << id.to_i
		end
	else
		print "Failed to load JSON file: #{base_filename}\nTry updating database!\n"
	end
end

def generate_locations_api_request(id)
	base_filename = "climb_directions/json/climb#{id}.json"
	if File.exist?(base_filename)
		# Get base coordinates
		base_coords = calculate_waypoints_for_json(base_filename)
		# Get coordinates + coordinates every 100m
		coords_with_distance = split_by_distance(base_coords, 100)
		splitted_coords = coords_with_distance
		# Get encoded polyline
		s = ""
		splitted_coords.each{|x|
			s << "#{x[0]},#{x[1]}|"
		}
		a = "curl -o \"climb_directions/elevations/climb#{id}.json\" \"https://maps.googleapis.com/maps/api/elevation/json?key=AIzaSyALd9yAbqV6Vr2DwdQjn-XMEr4x75XhLss&locations=#{s}\""
		a[-2] = ""
		File.open("climb_directions/elevrequests/climb#{id}.txt","wb"){|w| w.write(a) }
		$request_str << "#{a}\n"
		print "#{id} parsed\n"
	else
		print "Failed to load JSON file: #{base_filename}\nTry updating database!\n"
	end
end

#** save generated requests
def save_generated_requests
	File.open("elevations_requests.sh", "wb"){|w| w.write($request_str) }
	print "Data was written.\n"
end
#** save global SQL (deprecated)

def save_global_sql
	print "Saving .sql..\n"
	File.open("climb_directions/database.sql","wb"){|w| w.write($global_sql) }
end

=begin
begin
	# Parse IDs with multipart data
	$multipart_ids.each{|x|
		generate_locations_api_request(x)
	}
	save_generated_requests
	#add_local_climb_old(ARGV[0].to_i)
end
=end

begin
	Find.find('climb_directions/json').each{|x|
		next if File.directory?(x) || x.split(".")[-1] != "json"
		id = x.split("/climb")[-1].gsub(".json","")
		# 2199 is an ocean road, it's not climb! [I will fix this later]
		if id.to_i<=2201
			print "Skipping #{id}\n"
		else
			# Parse only multiparts
			unless $multipart_ids.include?(id.to_i)
				print "Skipping single part #{id}\n"
				next
			end
			print "Parsing #{id}\n"
			add_local_climb(id,200)
		end
	}
	print "Saving global SQL..\n"
	save_global_sql
	print "Bad climb ids: #{$bad_ids }\n"
	print $bad_climb_ids
	print "Done.\n"
end
