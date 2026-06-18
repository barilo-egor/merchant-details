// Требуется наличие следующих переменных в Jenkins:
// - SSH_CRED_ID - идентификатор SSH ключа
// - MERCHANT_DETAILS_DEPLOY_PATH - путь на сервере, куда необходимо расположить собранные проекты
// - MERCHANT_DETAILS_DEPLOY_HOST - IP адрес сервера, на который будут отправлены проекты
// - SSH_USER - пользователь SSH
// - SSH_PORT - порт SSH
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean test bootJar --no-daemon'
            }
        }
        stage('Deploy') {
            steps {
                sshagent([env.SSH_CRED_ID]) {
                    sh "scp -P ${SSH_PORT} build/libs/merchant-details.jar ${SSH_USER}@${MERCHANT_DETAILS_DEPLOY_HOST}:${MERCHANT_DETAILS_DEPLOY_PATH}/"
                    sh "ssh -p ${SSH_PORT} ${SSH_USER}@${MERCHANT_DETAILS_DEPLOY_HOST} 'cd /srv/merchant-details && docker rollout --timeout 120 merchant-details'"
                }
            }
        }
    }

    post {
        success {
            echo 'Сборка и деплой успешно завершены!'
        }
        failure {
            echo 'Ошибка при сборке или деплое.'
        }
    }
}