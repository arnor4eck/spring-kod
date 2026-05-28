import React from "react";
import { PieChart, Pie, Cell, Tooltip, Legend } from "recharts";
import "./ClassPieChart.scss";

interface ChartData {
  name: string;
  value: number;
  count: number;
  deficit: number;
}

interface ClassPieChartProps {
  data: ChartData[];
}

interface CustomTooltipProps {
  active?: boolean;
  payload?: Array<{
    payload: ChartData;
  }>;
}

const COLORS = [
  "#0088FE",
  "#00C49F",
  "#FFBB28",
  "#FF8042",
  "#8884D8",
  "#82CA9D",
];

const CustomTooltip: React.FC<CustomTooltipProps> = ({ active, payload }) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className="pie-chart__tooltip">
        <p className="pie-chart__tooltip-name">{data.name}</p>
        <p className="pie-chart__tooltip-value">Процент: {data.value}%</p>
        <p className="pie-chart__tooltip-count">Количество: {data.count}</p>
        <p className="pie-chart__tooltip-deficit">Дефицит: {data.deficit}</p>
      </div>
    );
  }
  return null;
};

const ClassPieChart: React.FC<ClassPieChartProps> = ({ data }) => {
  const chartData = data.map((item) => ({
    name: item.name,
    value: item.value,
    count: item.count,
    deficit: item.deficit,
  }));

  return (
    <div className="class-pie-chart">
      <PieChart width={400} height={400}>
        <Pie
          data={chartData}
          cx="50%"
          cy="50%"
          labelLine={false}
          outerRadius={150}
          fill="#8884d8"
          dataKey="value"
        >
          {chartData.map((_, index) => (
            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
          ))}
        </Pie>
        <Tooltip content={<CustomTooltip />} />
        <Legend />
      </PieChart>
      <div className="class-pie-chart__details">
        {chartData.map((item, index) => (
          <div key={index} className="class-pie-chart__detail-item">
            <span className="class-pie-chart__detail-name">{item.name}</span>
            <span className="class-pie-chart__detail-count">
              Объектов: {item.count}
            </span>
            <span className="class-pie-chart__detail-deficit">
              Дефицит: {item.deficit}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ClassPieChart;
