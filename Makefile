.PHONY: build test docker_build docker_deploy


build:
	mvn clean package -DskipTests=true

test:
	mvn test


docker_build:
	docker build -f src/main/docker/Dockerfile --build-arg GIT_COMMIT="$$CIRCLE_BRANCH/$$CIRCLE_SHA1" -t xamatech/deck:latest .

docker_deploy:
	docker tag xamatech/deck:latest xamatech/deck:$$CIRCLE_BRANCH
	echo $$DOCKER_PWD | docker login -u $$DOCKER_LOGIN --password-stdin
	docker push xamatech/deck:$$CIRCLE_BRANCH
