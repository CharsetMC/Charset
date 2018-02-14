pipeline {
	agent any
	stages {
		stage('Build') {
			steps {
				sh 'git submodule update --init --recursive'
				sh 'rm -f private.gradle'
				sh './gradlew setupCiWorkspace clean build --refresh-dependencies'
				archive 'build/libs/*jar'
			}
		}
		stage('Deploy') {
			steps {
				withCredentials([file(credentialsId: 'privateGradleNoSnapshot', variable: 'PRIVATEGRADLE')]) {
					sh '''
						cp "$PRIVATEGRADLE" private.gradle
						./gradlew upload
					'''
				}
			}
		}
	}
}
