# Estágio de Build
FROM eclipse-temurin:17-jdk-focal AS build

WORKDIR /app

# Instala o Maven
# curl e unzip são necessários para baixar e descompactar o Maven
RUN apt-get update && apt-get install -y curl unzip \
    && curl -fSL https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip -o maven.zip \
    && unzip maven.zip \
    && mv apache-maven-3.9.6 /opt/maven \
    && rm maven.zip \
    && ln -s /opt/maven/bin/mvn /usr/local/bin/mvn

# Define a variável de ambiente M2_HOME (opcional, mas boa prática)
ENV M2_HOME /opt/maven
ENV PATH="${M2_HOME}/bin:${PATH}"

# Copia o arquivo pom.xml
COPY pom.xml .

# Baixa as dependências do Maven
RUN mvn dependency:go-offline -B

# Copia todo o código fonte
COPY src ./src

# Compila o projeto e empacota o JAR
RUN mvn clean package -DskipTests

# Estágio de Execução
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

EXPOSE 8080

# Copia o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]