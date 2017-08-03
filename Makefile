complete: project.clj src/*
	lein -U deps
	lein run

clear:
	rm -r target
