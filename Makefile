complete: project.clj src/*
	lein deps
	lein run

test: project.clj src/* test/* 
	lein test

clear:
	rm -r target
