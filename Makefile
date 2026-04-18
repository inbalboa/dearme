TAG := $(shell sh -c 'date +%Y%m%d; git rev-list HEAD --count' | paste -sd .)

build:
	@printf "==> Generating signed APK...\n"
	@sh gradlew clean
	@sh gradlew assembleRelease

lint:
	@printf "==> Linting...\n"
	@sh gradlew lint

clean:
	@printf "==> Cleaning...\n"
	@sh gradlew clean

install-debug:
	@printf "==> Installing debug...\n"
	@sh gradlew installDebug

install:
	@printf "==> Installing release...\n"
	@sh gradlew installRelease

uninstall:
	@printf "==> Uninstalling...\n"
	@sh gradlew uninstallAll

test:
	@printf "==> Unit testing...\n"
	@sh gradlew test

testui:
	@printf "==> UI testing...\n"
	@sh gradlew connectedAndroidTest

run:
	@printf "==> Running app...\n"
	@adb shell am start -n com.github.inbalboa.dearme/.MainActivity

deploy: install run
	@printf "==> Deploying...\n"

clear-app-data:
	@printf "==> Clearing app data...\n"
	adb shell pm clear com.github.inbalboa.dearme

stop:
	@printf "==> Stopping Gradle...\n"
	@sh gradlew --stop

version:
	@printf 'DearMe %s\n' "$(TAG)"

release:
	@printf "v$(TAG)\n"
	@printf "==> tagging...\n"
	@git tag -a "v$(TAG)" -m "Release $(TAG)"
	@printf "==> pushing...\n"
	@git push --atomic origin main "v$(TAG)"
	@printf "\nReleased at %s\n\n" "`date`"

.DEFAULT_GOAL := build
.PHONY: install-debug install uninstall test testui run deploy build lint clean stop release version
