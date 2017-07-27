complete: project.clj src/*
	lein deps
	lein run

clear:
	rm -r target
