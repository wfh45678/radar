const path = require('path');
const webpack = require('webpack');
const merge = require('webpack-merge');
const HtmlWebPackPlugin = require('html-webpack-plugin');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

const baseWebpackConfig = require('./webpack.config.base.js');

const webpackConfig = merge(baseWebpackConfig, {
  entry: ['@babel/polyfill', './src/index.jsx'],
  externals:{},
  output: {
    path: path.resolve('dist'),
    filename: '[name].[hash:8].js',
    publicPath: './',
    libraryTarget: 'umd'
  },
  plugins: [
    new UglifyJSPlugin({
      uglifyOptions: {
        compress: {
          warnings: false,
          drop_debugger: true,
          drop_console: true
        },
        sourceMap: true
      }
    }),
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
      filename: './index.html',
      minify: {
        // 压缩HTML文件
        removeComments: true, // 移除HTML中的注释
        collapseWhitespace: false // 删除空白符与换行符
      }
    }),
    new webpack.DefinePlugin({
      'SERVICE_URL': JSON.stringify("")
    })
  ],
  mode: 'production'
});


module.exports = webpackConfig;
