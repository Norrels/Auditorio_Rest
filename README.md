![Slide 16_9 - 4](https://user-images.githubusercontent.com/94193637/211572824-5c12ae2f-2ad9-44b2-8a5e-1ccbd71734d7.png)


## Introdução 
  O projeto foi desenvolvido para conclusão do curso técnico de desenvolvimento de sistemas, esse projeto compõe-se a parte back-end
da aplicação junto com a API de email, ao todo foram 4/5 meses de trabalho


## The code
- A base desse projeto é o Java <img align="center" alt="Java-Logo" height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-plain-wordmark.svg" title=Java /> 
- Foi utilizado o Framework Spring-Boot <img align="center" alt="Spring-Logo" height="30" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original-wordmark.svg" title=Spring-Boot /> que possibilitou eficiência em
  - Criar rotas http dentro do projeto
  - Fazer queries no banco de dados por meio dos repositories
  - Gerenciar dependencias com o maven
  - Hibernate um ORN (Object Relational Mapping) utilizado para salvar os registro no banco, e criar as tabelas por meio das classes models
  - Criptografia hash com a dep. Guava, nas senhas e outros dados sensíveis do usuário
- Também foi utilizado MySQL <img align="center" alt="Spring-Logo" height="25" width="25" src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg" title=MySQL />
  - O banco de dados utilizado nesse projeto foi o MySQL, ao clonar o projeto caso queira é possível alterar ao banco de dados de sua preferência, caso contrario é necessário informar o endereço do banco no arquivo config.java
    
    
## Features
A ideia inicial do projeto era resolver a questão "quem está, quem estava e quem vai estar no auditório" antes de o sistema ser criado, toda essa logística era feita casualmente feita entre conversas, o que não era muito efetivo, haja vista que as chances de duas pessoas quererem utilizar o auditório nos mesmo dia e horário não era baixas, o sistema surge com a necessidade de resolver esse problema

- [x] Agora o administrador aprova a requisição do usuário, trazendo assim um controle maior de quem vai utilizá-lo
- [x] As solicitações se tornam eventos antes de serem realmente marcadas, no calendário
- [x] E possível controlar o acesso dos usuários pelo usuário do suporte
- [x] Todas as movimentações, criações, alterações, exclusões dos eventos são alertadas para os superiores por meio do e-mail
- [x] Os dados sensíveis do usuário são criptografados
- [x] Login com o TokenJWT
- [x] Um usuário nunca vai conseguir marca um evento no mesmo dia e horário de outro evento já marcado

  
  
### Agradecimentos
  Um muito obrigado a toda equipe mas principalmente ao meu professor José Roberto Chile, e ao <a href="https://github.com/Arthur-Rosa">Arthur Rosa</a><br/> 
  Acesse o front end aqui <a href="https://github.com/Arthur-Rosa/FrontEnd-JS">Auditorio Front-End</a>
