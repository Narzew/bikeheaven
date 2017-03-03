require 'json'

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
data = File.read('json/sampleelev.json')
json_data = JSON.parse(data)
json_data['results'].each{|x|
	$points_data << [x['location']['lat'],x['location']['lng']]
}
old_x = 0.0
old_y = 0.0
count = 0
$points_data.each{|x|
	if old_x == 0.0 || old_y == 0.0
		old_x = x[0]
		old_y = x[1]
		count+=1
		next
	else
		print distance [old_x,old_y],[x[0],x[1]]
		print "\n"
		old_x = x[0]
		old_y = x[1]
		count += 1
	end
}
print "Points count: #{count}\n"
