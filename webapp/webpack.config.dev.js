const path = require('path');
const webpack = require('webpack');
const merge = require('webpack-merge');
const portfinder = require('portfinder');
const FriendlyErrorsPlugin = require('friendly-errors-webpack-plugin');
const HtmlWebPackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const baseWebpackConfig = require('./webpack.config.base.js');

const devWebpackConfig = merge(baseWebpackConfig, {
  devtool: 'cheap-module-source-map',
  entry: ['@babel/polyfill', './src/index.jsx'],
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'app.bundle.js'
  },
  devServer: {
    contentBase: path.join(__dirname, 'dist'),
    port: 8080,
    disableHostCheck: true,
    host: '0.0.0.0',
    compress: true,
    inline: true,
    hot: true,
    overlay: true,
    disableHostCheck: true, //  新增该配置项
    proxy: [{
        context: ["/services/v1/"],
        target: "http://104.128.89.231:8080",
        changeOrigin: true,
        secure: false,
        onProxyRes: function (proxyRes, req, res) { //
            // console.log(proxyRes)
            let proxyHost = proxyRes.req.getHeader('host');
            let proxyPath = proxyRes.req.path;
            //console.log(host, path)
            console.log(`Proxy ${req.get('host')}${req.path} -> ${proxyHost}${proxyPath}`)
        }
    }]
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new MiniCssExtractPlugin({
      // Options similar to the same options in webpackOptions.output
      // both options are optional
      // filename: devMode ? '[name].css' : '[name].[hash].css',
      // chunkFilename: devMode ? '[id].css' : '[id].[hash].css',
      filename: 'index.[hash:8].css',
      // chunkFilename: '[id].[chunkhash].css'
    }),
    new HtmlWebPackPlugin({
      template: path.resolve('./src/index.html'),
      filename: './index.html'
    }),
    new webpack.DefinePlugin({
      'SERVICE_URL': JSON.stringify("http://localhost:3000")
    })
  ],
  mode: 'development'
});


module.exports = new Promise((resolve, reject) => {
  portfinder.basePort = process.env.PORT || devWebpackConfig.devServer.port;
  portfinder.getPort((err, port) => {
    if (err) {
      reject(err);
    } else {
      // publish the new Port, necessary for e2e tests
      process.env.PORT = port;
      // add port to devServer config
      devWebpackConfig.devServer.port = port;

      // Add FriendlyErrorsPlugin
      devWebpackConfig.plugins.push(new FriendlyErrorsPlugin({
        compilationSuccessInfo: {
          messages: [`Your application is running here: http://${devWebpackConfig.devServer.host}:${port}`]
        }
      }));

      resolve(devWebpackConfig);
    }
  });
});

exports.config = devWebpackConfig;