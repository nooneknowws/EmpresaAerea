require("dotenv-safe").config();
const amqp = require("amqplib/callback_api");
const jwt = require("jsonwebtoken");
var http = require("http");
const express = require("express");
const httpProxy = require("express-http-proxy");
const app = express();
var cookieParser = require("cookie-parser");
var bodyParser = require("body-parser");
var logger = require("morgan");
const helmet = require("helmet");
const cors = require("cors");
const nodemailer = require("nodemailer");

app.use(
  cors({
    origin: ["http://localhost:4200"],
    methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"],
    allowedHeaders: ["Content-Type", "x-access-token", "Authorization", "refresh-token"],
    credentials: true,
  })
);

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

const authServiceProxy = httpProxy("http://localhost:5000", {
  proxyReqPathResolver: function (req) {
    if (req.path === "/login") return "/login";
    if (req.path === "/logout") return "/logout";
    if (req.path === "/refresh-token") return "/refresh-token";
    return req.path;
  },
  proxyReqBodyDecorator: function (bodyContent, srcReq) {
    try {
      if (srcReq.path === "/login") {
        const loginRequest = {
          email: bodyContent.email,
          senha: bodyContent.senha,
        };
        console.log("Processing login request");
        return loginRequest;
      }

      if (srcReq.path === "/logout") {
        console.log("Processing logout request with token:", bodyContent.token);
        const logoutRequest = {
          token: bodyContent.token,
        };
        return logoutRequest;
      }

      if (srcReq.path === "/refresh-token") {
        const refreshTokenRequest = {
          refreshToken: bodyContent.refreshToken
        };
        console.log("Processing refresh token request");
        return refreshTokenRequest;
      }

      return bodyContent;
    } catch (e) {
      console.log("Error processing request:", e);
      return bodyContent;
    }
  },
  proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
    proxyReqOpts.headers["Content-Type"] = "application/json";
    proxyReqOpts.method = srcReq.method;
    
    const token = srcReq.headers["x-access-token"];
    if (token) {
      proxyReqOpts.headers["x-access-token"] = token;
    }
    
    return proxyReqOpts;
  },
  userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
    const responseString = proxyResData.toString("utf8");
    console.log(`Received response from Auth service:`, responseString);

    try {
      const jsonResponse = JSON.parse(responseString);
    
      userRes.status(proxyRes.statusCode);
      
      return jsonResponse;
    } catch (error) {
      console.log("Error parsing response:", error);
      return responseString;
    }
  },
});


const voosServiceProxy = httpProxy("http://localhost:5001");
const reservasServiceProxy = httpProxy("http://localhost:5005");
const clientesServiceProxy = httpProxy("http://localhost:5003");
const FuncionarioServiceProxy = httpProxy("http://localhost:5007")

async function mailServer(req, res, next) {
  const originalEnd = res.end;

  res.end = async function (chunk, encoding) {
    try {
      const statusCode = res.statusCode;

      if (statusCode >= 200 && statusCode < 300) {
        const { nome, email, senha } = req.body;

        const transporter = nodemailer.createTransport({
          host: "smtp.sendgrid.net",
          port: 587,
          auth: {
            user: process.env.EMAIL_USER,
            pass: process.env.EMAIL_PASSWORD,
          },
        });

        const mailOptions = {
          from: "empresaaerea.dac.ufpr@gmail.com",
          to: email,
          subject: "Bem-vindo(a) a nossa companhia de passagens aereas!",
          text: `Olá ${nome},\n\nSeu cadastro foi realizado com sucesso!\n\nSua senha de acesso é: ${senha}\n\nAtenciosamente,\nEquipe 4 DAC - UFPR`,
        };

        await transporter.sendMail(mailOptions);
        console.log(`Email sent to ${email} after successful registration A sennha é ${senha}`);
      }
      originalEnd.call(res, chunk, encoding);
    } catch (error) {
      console.error("Error in mailServer:", error);
      originalEnd.call(res, chunk, encoding);
    }
  };

  next();
}

function verifyPerfil(req, res, next) {
  const perfil = req.query.perfil;
  const status = req.query.statusFunc;

  if (perfil === "Funcionario" && status === "ATIVO") {
    next();
  } else {
    return res.status(403).json({
      auth: false,
      message: "Você não tem permissão para acessar esses dados.",
    });
  }
}

function verifyJWT(req, res, next) {
  const token = req.headers["x-access-token"];

  if (!token) {
    return res.status(401).json({
      auth: false,
      message: "Token não fornecido",
    });
  }

  jwt.verify(token, process.env.SECRET, function (err, decoded) {
    if (err) {
      return res.status(401).json({
        auth: false,
        message: "Falha ao autenticar token.",
      });
    }

    req.userId = decoded.id;
    next();
  });
}
app.get("/session/check", verifyJWT, (req, res, next,)=> {
  const token = req.headers["x-access-token"];

  if (!token) {
    return res.status(401).json({
      status: "error",
      message: "No token provided",
    });
  }

  const sessionCheckProxy = httpProxy("http://localhost:5000/session", {
    proxyReqOptDecorator: function (proxyReqOpts, srcReq) {
      proxyReqOpts.headers["Content-Type"] = "application/json";
      proxyReqOpts.method = "GET";
      proxyReqOpts.headers["x-access-token"] = token;
      proxyReqOpts.headers["Access-Control-Allow-Origin"] =
        "http://localhost:4200";
      proxyReqOpts.headers["Access-Control-Allow-Methods"] = "GET,OPTIONS";
      proxyReqOpts.headers["Access-Control-Allow-Headers"] =
        "Content-Type, x-access-token";

      return proxyReqOpts;
    },
    userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
      userRes.header("Access-Control-Allow-Origin", "http://localhost:4200");
      userRes.header("Access-Control-Allow-Credentials", "true");

      const responseString = proxyResData.toString("utf8");
      console.log("Session check response:", responseString);

      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error("Error parsing session check response:", error);
        return {
          status: "error",
          message: "Error processing session check response",
        };
      }
    },
  });

  sessionCheckProxy(req, res, next);
});

app.post("/login", (req, res, next) => {
  if (!req.body.email || !req.body.senha) {
    return res.status(400).json({
      error: "Email e senha são obrigatórios",
    });
  }
  authServiceProxy(req, res, next);
});

app.post("/logout", (req, res, next) => {
  const token = req.headers["x-access-token"] || req.body.token;
  
  if (!token) {
    return res.status(400).json({
      status: "error",
      message: "Token não fornecido no corpo da requisição ou headers",
    });
  }
  req.body.token = token;
  req.headers["x-access-token"] = token;

  console.log("Logout request received with token:", token);
  authServiceProxy(req, res, next);
});
app.options("/logout", cors());

app.post("/refresh-token", (req, res, next) => {
  if (!req.body.refreshToken) {
    return res.status(400).json({
      status: "error",
      message: "Refresh token is required"
    });
  }
  
  authServiceProxy(req, res, next);
});

//aqui vai os HTTP da vida, que comunica com os MS->
// MS-VOOS
//Listar os aeroportos
app.get("/api/aeroportos", verifyJWT, (req,res,next) => {
  voosServiceProxy(req,res, next)
})
// POST aeroportos
app.post("/api/aeroportos", (req, res, next) => {
  voosServiceProxy(req, res, next, {
    proxyReqPathResolver: (req) => '/api/aeroportos/',
    proxyReqBodyDecorator: (bodyContent) => bodyContent,
    proxyReqOptDecorator: function(proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      proxyReqOpts.method = 'POST';
      return proxyReqOpts;
    }
  });
});
// Rota para listar todos os voos (GET)
app.get("/voos", verifyJWT, (req, res, next) => {
  // TODO: Implementar a verificação do token JWT (verifyJWT) na chamada
  voosServiceProxy(req, res, next);
});

// Rota para listar um voo pelo ID (GET)
app.get("/voos/:id", verifyJWT, (req, res, next) => {
  voosServiceProxy(req, res, next, {
    proxyReqPathResolver: (req) => `/voos/${req.params.id}`,
  });
});

// Rota para inserir um novo voo (POST)
app.post("/voos", verifyJWT, (req, res, next) => {
  voosServiceProxy(req, res, next, {
    proxyReqBodyDecorator: (bodyContent) => bodyContent,
  });
});
// ROTA PARA REALIZAR O VOO
app.patch("/voos/:id/status", cors(), verifyJWT, (req, res, next) => {
  console.log('Received PATCH request for flight status update:', {
    flightId: req.params.id,
    status: req.query.status
  });

  voosServiceProxy(req, res, next, {
    proxyReqPathResolver: (req) => `/voos/${req.params.id}/status?status=${req.query.status}`,
    proxyReqOptDecorator: function(proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      proxyReqOpts.method = 'PATCH';
      return proxyReqOpts;
    }
  });
});
//FILTRO DE VOOS POR AEROPORTO ORIGEM/DESTINO
app.get("/voos/filter",  verifyJWT, (req, res, next) => {
  voosServiceProxy(req, res, {
    proxyReqPathResolver: (req) => {
      return `/voos/filter?origem=${req.query.origem}&destino=${req.query.destino}`;
    },
    userResDecorator: function(proxyRes, proxyResData, userReq, userRes) {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from voos filter service:`, responseString);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing voos filter response:', error);
        return {
          error: 'Invalid response from voos service'
        };
      }
    }
  });
});
// ROTA PARA CANCELAR O VOO
app.patch("/voos/:id/cancelar", cors(), verifyJWT, (req, res, next) => {
  console.log('Received PATCH request for flight cancellation:', {
    flightId: req.params.id
  });

  voosServiceProxy(req, res, next, {
    proxyReqPathResolver: (req) => `/voos/${req.params.id}/cancelar`,
    proxyReqOptDecorator: function(proxyReqOpts, srcReq) {
      proxyReqOpts.headers['Content-Type'] = 'application/json';
      proxyReqOpts.method = 'PATCH';
      return proxyReqOpts;
    }
  });
});

// MS-RESERVAS
// CONFIRMAR RESERVA
app.put("/reservas/:id/checkin", verifyJWT, (req, res, next) => {
  reservasServiceProxy(req, res, next, {  // Added next parameter
    proxyReqPathResolver: (req) => `/reservas/${req.params.id}/checkin`,
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from reservas confirmation service: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing reservas confirmation response:', error);
        return {
          error: 'Invalid response from reservas service'
        };
      }
    }
  });
});
// CONFIRMAR EMBARQUE
app.put("/reservas/:id/embarque", verifyJWT, (req, res, next) => {
  reservasServiceProxy(req, res, {
    proxyReqPathResolver: (req) => `/reservas/${req.params.id}/embarque`,
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from reservas embarque service: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing reservas embarque response:', error);
        return {
          error: 'Invalid response from reservas service'
        };
      }
    }
  });
});

// CANCELAR RESERVA
app.put("/reservas/:id/cancelar", verifyJWT, (req, res, next) => {
  reservasServiceProxy(req, res, {
    proxyReqPathResolver: (req) => `/reservas/${req.params.id}/cancelar`,
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from reservas cancellation service: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing reservas cancellation response:', error);
        return {
          error: 'Invalid response from reservas service'
        };
      }
    }
  });
});
// RESERVAS POR CLIENTE E 48HORAS
app.get("/reservas/cliente/:clienteId/filter-data", verifyJWT, (req, res, next) => {
  reservasServiceProxy(req, res, {
    proxyReqPathResolver: (req) => `/reservas/cliente/${req.params.clienteId}/filter-data`,
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from reservas service for upcoming reservations of client ${req.params.clienteId}: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing reservas service response:', error);
        return {
          error: 'Invalid response from reservas service'
        };
      }
    }
  });
});
// GET RESERVAS POR VOO
app.get("/reservas/voo/:vooId", verifyJWT, (req, res, next) => {
  reservasServiceProxy(req, res, {
    proxyReqPathResolver: (req) => `/reservas/voo/${req.params.vooId}`,
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from reservas service for flight ${req.params.vooId}: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing reservas service response:', error);
        return {
          error: 'Invalid response from reservas service'
        };
      }
    }
  });
});
// GET RESERVAS POR CODIGO
app.get("/reservas/codigo/:codigoReserva", verifyJWT, (req, res, next) => {
  reservasServiceProxy(req, res, {
    proxyReqPathResolver: (req) => `/reservas/codigo/${req.params.codigoReserva}`,
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from reservas service for code ${req.params.codigoReserva}: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing reservas service response:', error);
        return {
          error: 'Invalid response from reservas service'
        };
      }
    }
  });
});
// GET RESERVAS POR CLIENTE
app.get("/reservas/cliente/:clienteId", verifyJWT, (req, res, next) => {
  reservasServiceProxy(req, res, {
    proxyReqPathResolver: (req) => `/reservas/cliente/${req.params.clienteId}`,
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from reservas service for client ${req.params.clienteId}: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing reservas service response:', error);
        return {
          error: 'Invalid response from reservas service'
        };
      }
    }
  });
});
// GET RESERVAS POR ID
app.get("/reservas/:id", verifyJWT, (req, res, next) => {
  reservasServiceProxy(req, res, {
    proxyReqPathResolver: (req) => `/reservas/${req.params.id}`,
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from reservas service for ID ${req.params.id}: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing reservas service response:', error);
        return {
          error: 'Invalid response from reservas service'
        };
      }
    }
  });
});
// CRIAR RESERVAS
app.post("/reservas", verifyJWT, (req, res, next) => {
  reservasServiceProxy(req, res, {
    proxyReqPathResolver: () => '/reservas',
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from reservas service: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing reservas service response:', error);
        return {
          error: 'Invalid response from reservas service'
        };
      }
    }
  });
});
// MS-CLIENTES
//cadastro
app.post("/clientes/cadastro", mailServer, (req, res, next) => {
  clientesServiceProxy(req, res, next);
});
//busca
app.get("/clientes/busca", verifyJWT, verifyPerfil, (req, res, next) => {
  clientesServiceProxy(req, res, next);
});
app.get("/clientes/busca/:id", verifyJWT, (req, res, next) => {
  clientesServiceProxy(req, res, {
    proxyReqPathResolver: (req) => `/clientes/busca/${req.params.id}`, // Forward the correct path
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString("utf8");
      console.log(`Response from client service: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse; 
      } catch (error) {
        console.error("Error parsing client service response:", error);
        return {
          error: "Invalid response from client service",
        };
      }
    },
  });
});
//Adicionar milhas
app.post("/api/milhas", verifyJWT, (req, res, next) => {
  clientesServiceProxy(req, res, {
    proxyReqPathResolver: () => '/api/milhas',
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from milhas transaction: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing milhas transaction response:', error);
        return {
          error: 'Invalid response from milhas service'
        };
      }
    }
  });
});

//Transações por cliente
app.get("/api/milhas/:clienteId", verifyJWT, (req, res, next) => {
  clientesServiceProxy(req, res, {
    proxyReqPathResolver: (req) => `/api/milhas/milhas/${req.params.clienteId}`,
    userResDecorator: (proxyRes, proxyResData, userReq, userRes) => {
      const responseString = proxyResData.toString('utf8');
      console.log(`Response from milhas service: ${responseString}`);
      try {
        const jsonResponse = JSON.parse(responseString);
        return jsonResponse;
      } catch (error) {
        console.error('Error parsing milhas service response:', error);
        return {
          error: 'Invalid response from milhas service'
        };
      }
    }
  });
});



// MS-FUNCIONARIOS
app.post("/funcionarios/cadastro", mailServer, (req, res, next) => {
  FuncionarioServiceProxy(req, res, next);
});
app.get("/funcionarios", verifyJWT, (req, res, next) => {
  FuncionarioServiceProxy(req, res, next);
});
app.get("/funcionarios/:id", verifyJWT, (req, res, next) => {
  console.log(`Receiving request for funcionario ID: ${req.params.id}`);

  FuncionarioServiceProxy(req, res, next, {
    proxyReqPathResolver: (req) => {
      const path = `/funcionarios/${req.params.id}`;
      console.log(`Forwarding to path: ${path}`);
      return path;
    },
    userResDecorator: function (proxyRes, proxyResData, userReq, userRes) {
      console.log(
        `Response from funcionario service:`,
        proxyResData.toString("utf8")
      );
      return proxyResData;
    },
  });
});
app.put("/funcionarios/edit/:id", verifyJWT, (req, res, next) => {
  FuncionarioServiceProxy(req, res, next, {
    proxyReqPathResolver: (req) => `/funcionarios/edit/${req.params.id}`,

    proxyReqBodyDecorator: (bodyContent) => bodyContent,
  });
});
app.put("/funcionarios/status/:id", verifyJWT, (req, res, next) => {
  FuncionarioServiceProxy(req, res, next, {
    proxyReqPathResolver: (req) => `/funcionarios/status/${req.params.id}`,
    proxyReqBodyDecorator: (bodyContent) => bodyContent,
  });
});

app.use(logger("dev"));
app.use(helmet());
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());

app.options(
  "/session/check",
  cors({
    origin: "http://localhost:4200",
    methods: ["GET"],
    allowedHeaders: ["Content-Type", "x-access-token"],
    credentials: true,
  })
);
app.options('/voos/:id', cors());

var server = http.createServer(app);
server.listen(3000);
