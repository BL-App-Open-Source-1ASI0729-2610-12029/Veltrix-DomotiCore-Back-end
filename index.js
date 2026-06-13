const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const swaggerDocument = YAML.load('./openapi.yaml');

app.use('/swagger-ui.html', swaggerUi.serve, swaggerUi.setup(swaggerDocument));
