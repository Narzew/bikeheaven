start_value = 4749
repetitions = 2500

repetitions.times{|x|
	x = x+start_value
	print "Downloading climb #{x}\n"
	system("ruby download_climb_directions.rb #{x}\n")
}
