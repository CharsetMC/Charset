node {
	checkout scm
	sh './gradlew setupCiWorkspace clean build'
	archive 'build/libs/*jar'
}
