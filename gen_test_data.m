
out_x = 'test_features.json';
out_y = 'test_prices.json';

N = 1000;
D = 7;

X = zeros(N,D);

X(:,1) = linspace(1,10,N);
X(:,2) = linspace(1,4,N) .^3;
X(:,3) = sin(linspace(1,10,N));
X(:,4) = sin(linspace(1,2,N));
X(:,5) = rand(N, 1);
X(:,6) = randn(N,1);
X(:,7) = [1:N];

Y = zeros(1,N);

for i = 1:N
   Y(i) = X(i,1) + 0.01 * X(i,2) + 2 * 0.4*X(i,3) - 0.2 * X(i,4) + 0.01*X(i,5) + 0.2*X(i,6) + 0.1 * randn(); 
end

plot(Y);
file = fopen(out_x, 'w');
for i = 1 : N
    fprintf(file, '%i {', i);    
    for d = 1:D
        
        fprintf(file, '%i : %f', d, X(i,d));
        if d ~= D
            fprintf(file, ',');
        end
    end
    fprintf(file, '}\n');
end

fclose(file);

file = fopen(out_y, 'w');
for i = 1:N
    fprintf(file, '%f\n', Y(i));
end

fclose(file);